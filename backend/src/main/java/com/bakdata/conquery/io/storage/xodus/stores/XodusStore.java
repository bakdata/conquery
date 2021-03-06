package com.bakdata.conquery.io.storage.xodus.stores;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.bakdata.conquery.io.storage.IStoreInfo;
import com.google.common.primitives.Ints;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XodusStore {
	private final Store store;
	private final Environment environment;
	private final long timeoutHalfMillis; // milliseconds
	private final Collection<Store>  openStores;
	private final Consumer<Environment> envCloseHook;
	private final Consumer<Environment> envRemoveHook;

	public XodusStore(Environment env, IStoreInfo storeId, Collection<Store> openStoresInEnv, Consumer<Environment> envCloseHook, Consumer<Environment> envRemoveHook) {
		// Arbitrary duration that is strictly shorter than the timeout to not get interrupted by StuckTxMonitor
		this.timeoutHalfMillis = env.getEnvironmentConfig().getEnvMonitorTxnsTimeout()/2;
		this.environment = env;
		this.openStores = openStoresInEnv;
		this.envCloseHook = envCloseHook;
		this.envRemoveHook = envRemoveHook;
		this.store = env.computeInTransaction(
			t->env.openStore(storeId.getName(), StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, t)
		);
		openStoresInEnv.add(store);
	}
	
	public boolean add(ByteIterable key, ByteIterable value) {
		return environment.computeInTransaction(t -> store.add(t, key, value));
	}

	public ByteIterable get(ByteIterable key) {
		return environment.computeInReadonlyTransaction(t -> store.get(t, key));
	}

	/**
	 * Iterate over all key-value pairs in a consistent manner.
	 * The transaction is read only!
	 * @param consumer function called for-each key-value pair.
	 */
	public void forEach(BiConsumer<ByteIterable, ByteIterable> consumer) {
		AtomicReference<ByteIterable> lastKey = new AtomicReference<>();
		AtomicBoolean done = new AtomicBoolean(false);
		while(!done.get()) {
			environment.executeInReadonlyTransaction(t -> {
				try(Cursor c = store.openCursor(t)) {
					//try to load everything in the same transaction
					//but keep within half of the timeout time
					long start = System.currentTimeMillis();
					//search where we left of
					if(lastKey.get() != null) {
						c.getSearchKey(lastKey.get());
					}
					while(System.currentTimeMillis()-start < timeoutHalfMillis) {
						if(!c.getNext()) {
							done.set(true);
							return;
						}
						lastKey.set(c.getKey());
						consumer.accept(lastKey.get(), c.getValue());
					}
				}
			});
		}
	}

	public boolean update(ByteIterable key, ByteIterable value) {
		return environment.computeInTransaction(t -> store.put(t, key, value));
	}
	
	public boolean remove(ByteIterable key) {
		return environment.computeInTransaction(t -> store.delete(t, key));
	}

	public int count() {
		return Ints.checkedCast(environment.computeInReadonlyTransaction(store::count));
	}


	public void clear() {
		environment.executeInExclusiveTransaction(t -> {
			Cursor cursor = store.openCursor(t);
			while(cursor.getNext()){
				cursor.deleteCurrent();
			}
		});
	}

	public void remove() {
		if (!environment.isOpen()) {
			log.debug("While removing store: Environment is already closed for {}", this);
			return;
		}
		log.debug("Removing store {} from environment {}", store, environment.getLocation());
		environment.executeInTransaction(t -> environment.removeStore(store.getName(),t));
		close();
		if (openStores.isEmpty()){
			// Last Store closes the Environment
			log.info("Removed last XodusStore in Environment. Removing Environment as well: {}", environment.getLocation());
			envRemoveHook.accept(environment);
		}
	}

	public void close() {
		if (!environment.isOpen()) {
			log.debug("While closing store: Environment is already closed for {}", this);
			return;
		}
		if (!openStores.remove(store)) {
			log.info("Closed XodusStore: {}", this);
			return;
		}
		if (openStores.isEmpty()){
			// Last Store closes the Environment
			log.info("Closed last XodusStore in Environment. Closing Environment as well: {}", environment.getLocation());
			envCloseHook.accept(environment);
		}
	}

	@Override
	public String toString() {
		return "XodusStore[" + environment.getLocation() + ":" +store.getName() +"}";
	}
}
