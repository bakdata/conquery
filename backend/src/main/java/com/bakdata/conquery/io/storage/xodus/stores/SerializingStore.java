package com.bakdata.conquery.io.storage.xodus.stores;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import javax.validation.Validator;

import com.bakdata.conquery.io.jackson.Injectable;
import com.bakdata.conquery.io.jackson.InternalOnly;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.io.jackson.JacksonUtil;
import com.bakdata.conquery.io.storage.IStoreInfo;
import com.bakdata.conquery.io.storage.Store;
import com.bakdata.conquery.models.config.XodusStoreFactory;
import com.bakdata.conquery.models.exceptions.ValidatorHelper;
import com.bakdata.conquery.util.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Throwables;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Key-value-store from {@link KEY} type values to {@link VALUE} values. ACID consistent, stored on disk using {@link jetbrains.exodus.env.Store} via {@link XodusStore}.
 * <p>
 * Values are (de-)serialized using {@linkplain ObjectMapper}.
 *
 * @param <KEY>   type of keys
 * @param <VALUE> type of values.
 */
@Slf4j
@ToString(of = "store")
public class SerializingStore<KEY, VALUE> implements Store<KEY, VALUE> {

	public static final String DUMP_FILE_EXTENTION = "json";

	/**
	 * Used for serializing keys.
	 */
	private final ObjectWriter keyWriter;

	/**
	 * Deserializer for keys
	 */
	private final ObjectReader keyReader;

	/**
	 * Serializer for values
	 */
	private final ObjectWriter valueWriter;

	/**
	 * Deserializer for values
	 */
	private ObjectReader valueReader;

	/**
	 * Optional validator used for serialization.
	 */
	private final Validator validator;

	/**
	 * The underlying store to write the values to.
	 */
	private final XodusStore store;

	/**
	 *
	 */
	private final Class<VALUE> valueType;

	/**
	 * Description of the store.
	 */
	private final IStoreInfo storeInfo;

	/**
	 * Validate elements on write
	 */
	private final boolean validateOnWrite;


	/**
	 * If set, all values that cannot be read are dumped as single files into this directory.
	 */
	private final File unreadableValuesDumpDir;

	private final boolean removeUnreadablesFromUnderlyingStore;

	private final ObjectMapper objectMapper;

	@SuppressWarnings("unchecked")
	public SerializingStore(XodusStoreFactory config, XodusStore store, Validator validator, IStoreInfo storeInfo, ObjectMapper objectMapper) {
		this.storeInfo = storeInfo;
		this.store = store;
		this.validator = validator;
		this.validateOnWrite = config.isValidateOnWrite();

		valueType = (Class<VALUE>) storeInfo.getValueType();

		this.objectMapper = objectMapper;

		valueWriter = objectMapper
							  .writerFor(valueType)
							  .withView(InternalOnly.class);

		valueReader = objectMapper
							  .readerFor(valueType)
							  .withView(InternalOnly.class);

		keyWriter = objectMapper
							.writerFor(storeInfo.getKeyType())
							.withView(InternalOnly.class);

		keyReader = objectMapper
							.readerFor(storeInfo.getKeyType())
							.withView(InternalOnly.class);

		removeUnreadablesFromUnderlyingStore = config.isRemoveUnreadableFromStore();

		// Prepare dump directory if there is one set in the config
		Optional<File> dumpUnreadable = config.getUnreadableDataDumpDirectory();
		if (dumpUnreadable.isPresent()) {
			unreadableValuesDumpDir = dumpUnreadable.get();
			if (!unreadableValuesDumpDir.exists()) {
				unreadableValuesDumpDir.mkdirs();
			}
			else if (!unreadableValuesDumpDir.isDirectory()) {
				throw new IllegalArgumentException(String.format("The provided path points to an existing file which is not a directory. Was: %s", unreadableValuesDumpDir
																																						   .getAbsolutePath()));
			}
		}
		else {
			unreadableValuesDumpDir = null;
		}
	}

	@Override
	public void add(KEY key, VALUE value) {
		if (!valueType.isInstance(value)) {
			throw new IllegalStateException("The element " + value + " is not of the required type " + valueType);
		}
		if (validateOnWrite) {
			ValidatorHelper.failOnError(log, validator.validate(value));
		}

		store.add(writeKey(key), writeValue(value));
	}

	@Override
	public VALUE get(KEY key) {
		ByteIterable binValue = store.get(writeKey(key));
		try {
			return readValue(binValue);
		}
		catch (Exception e) {
			dumpToFile(binValue, key.toString(), unreadableValuesDumpDir, storeInfo.getName(), objectMapper);
			if (removeUnreadablesFromUnderlyingStore) {
				remove(key);
				// Null seems to be an acceptable return value in this case
				return null;
			}
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Iterates a given consumer over the entries of this store.
	 * Depending on the {@link XodusStoreFactory} corrupt entries may be dump to a file and/or removed from the store.
	 * These entries are not submitted to the consumer.
	 */
	@SneakyThrows
	@Override
	public IterationStatistic forEach(BiConsumer<KEY, VALUE> consumer) {
		IterationStatistic result = new IterationStatistic();
		Set<ByteIterable> unreadables = new HashSet<>();

		final ExecutorService service = Executors.newWorkStealingPool();


		List<CompletableFuture<?>> futures = new ArrayList<>();

		store.forEach((rawKey, rawValue) -> {
			result.incrTotalProcessed();

			// We receive key/value from an IO thread, and deserialize them asynchronously.
			// Read-errors are expected we log and dump the values, then move on.

			final CompletableFuture<KEY> futureKey =
					CompletableFuture.supplyAsync(
							() -> {
								if (log.isTraceEnabled()) {
									log.trace("Reading Key `{}`", new String(rawKey.getBytesUnsafe()));
								}
								return readKey(rawKey);
							}, service)
									 .exceptionally(
											 exc -> {
												 log.warn("Could not parse Key `{}`", new String(rawKey.getBytesUnsafe()), log.isTraceEnabled() ? exc : null);

												 result.incrFailedKeys();
												 unreadables.add(rawKey);

												 return null;
											 });


			final CompletableFuture<VALUE> futureValue =
					CompletableFuture.supplyAsync(
							() -> {
								if (log.isTraceEnabled()) {
									log.trace("Reading Value for Key `{}`", new String(rawKey.getBytesUnsafe()));
								}

								return readValue(rawValue);
							},
							service
					)
									 .exceptionally(
											 exc -> {
												 // When we cannot read the value, we dump it using dumpToFile
												 final String keyOfDump = new String(rawKey.getBytesUnsafe());

												 log.warn("Could not parse Value for Key `{}`", keyOfDump, exc);

												 result.incrFailedValues();

												 unreadables.add(rawKey);

												 dumpToFile(rawValue, keyOfDump, unreadableValuesDumpDir, storeInfo.getName(), objectMapper);

												 return null;
											 });

			// Our providers return null on exception (the only effective way of shadowing exceptions), we therefore only pass on valid non-null key/value-pairs
			final CompletableFuture<Void> combined =
					futureKey.thenAcceptBothAsync(
							futureValue,
							(key, value) -> {
								if (key != null && value != null) {
									consumer.accept(key, value);
								}

							}, service
					)
							 .exceptionally(
									 exc -> {
										 // Exceptions here are serious, as they happen in our users and not us.
										 //TODO maybe don't catch them?
										 log.error("Unable to apply for-each consumer on Key {}", new String(rawKey.getBytesUnsafe()), exc);
										 unreadables.add(rawKey);

										 return null;
									 });

			// We gather all futures into a list to join on them
			futures.add(combined);

		});

		log.trace("All jobs submitted, waiting for {}", futures.size());

		// Wait for completion of all futures (exceptionally or not does not bother us here)
		futures.forEach(CompletableFuture::join);

		// Shutdown the service and await properly
		service.shutdown();

		while (!service.awaitTermination(30, TimeUnit.SECONDS)) {
			log.debug("Still waiting for {} loaders to finish.", this);
		}

		// Print some statistics
		int total = result.getTotalProcessed();
		log.debug(
				String.format(
						"While processing store %s:\n\tEntries processed:\t%d\n\tKey read failure:\t%d (%.2f%%)\n\tValue read failure:\t%d (%.2f%%)",
						storeInfo.getName(),
						total,
						result.getFailedKeys(),
						total > 0 ? (float) result.getFailedKeys() / total * 100 : 0,
						result.getFailedValues(),
						total > 0 ? (float) result.getFailedValues() / total * 100 : 0
				));

		// Remove corrupted entries from the store if configured
		if (removeUnreadablesFromUnderlyingStore) {
			log.warn("Removing {} unreadable elements from the store {}.", unreadables.size(), storeInfo.getName());
			unreadables.forEach(store::remove);
		}
		return result;
	}

	@Override
	public void update(KEY key, VALUE value) {
		if (!valueType.isInstance(value)) {
			throw new IllegalStateException("The element " + value + " is not of the required type " + valueType);
		}

		if (validateOnWrite) {
			ValidatorHelper.failOnError(log, validator.validate(value));
		}

		store.update(writeKey(key), writeValue(value));
	}

	@Override
	public void remove(KEY key) {
		log.trace("Removing value to key {} from Store[{}]", key, storeInfo.getName());
		store.remove(writeKey(key));
	}

	/**
	 * Serialize value with {@code valueWriter}.
	 */
	private ByteIterable writeValue(VALUE value) {
		return write(value, valueWriter);
	}

	/**
	 * Serialize key with {@code keyWriter}.
	 */
	private ByteIterable writeKey(KEY key) {
		return write(key, keyWriter);
	}

	/**
	 * Deserialize value with {@code valueReader}.
	 */
	private VALUE readValue(ByteIterable value) {
		return read(valueReader, value);
	}

	/**
	 * Deserialize value with {@code keyReader}.
	 */
	private KEY readKey(ByteIterable key) {
		return read(keyReader, key);
	}

	/**
	 * Try writing object with writer.
	 */
	private ByteIterable write(Object obj, ObjectWriter writer) {
		try {
			byte[] bytes = writer.writeValueAsBytes(obj);
			if (log.isTraceEnabled()) {
				String json = JacksonUtil.toJsonDebug(bytes);
				log.trace("Written Messagepack ({}): {}", valueType.getName(), json);
			}
			return new ArrayByteIterable(bytes);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to write " + obj, e);
		}
	}

	/**
	 * Try read value with reader.
	 */
	private <T> T read(ObjectReader reader, ByteIterable obj) {
		if (obj == null) {
			return null;
		}
		try {
			return reader.readValue(obj.getBytesUnsafe(), 0, obj.getLength());
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to read " + JacksonUtil.toJsonDebug(obj.getBytesUnsafe()), e);
		}
	}

	/**
	 * Dumps the content of an unreadable value to a file as a json (it tries to parse it as an object and than tries to dump it as a json).
	 *
	 * @param obj               The object to dump.
	 * @param keyOfDump         The key under which the unreadable value is accessible. It is used for the file name.
	 * @param unreadableDumpDir The director to dump to. The method assumes that the directory exists and is okay to write to.
	 * @param storeName         The name of the store which is also used in the dump file name.
	 */
	private static void dumpToFile(@NonNull ByteIterable obj, @NonNull String keyOfDump, File unreadableDumpDir, @NonNull String storeName, ObjectMapper objectMapper) {

		if (unreadableDumpDir == null) {
			return;
		}

		// Create dump filehandle
		File dumpfile = new File(unreadableDumpDir, makeDumpfileName(keyOfDump, storeName));
		if (dumpfile.exists()) {
			log.trace("Abort dumping of file {} because it already exists.", dumpfile);
			return;
		}
		// Write dump
		try {
			log.info("Dumping value of key {} to {} (because it cannot be deserialized anymore).", keyOfDump, dumpfile.getCanonicalPath());
			JsonNode dump = objectMapper.readerFor(JsonNode.class).readValue(obj.getBytesUnsafe(), 0, obj.getLength());
			Jackson.MAPPER.writer().writeValue(dumpfile, dump);
		}
		catch (Exception e) {
			log.error("Unable to dump unreadable value of key `{}` to file `{}`", keyOfDump, dumpfile, e);
		}
	}

	/**
	 * Generates a valid file name from the key of the dump object, the store and the current time.
	 * However, it does not ensure that there is no file with such a name.
	 */
	private static String makeDumpfileName(String keyOfDump, String storeName) {
		return FileUtil.SAVE_FILENAME_REPLACEMENT_MATCHER.matcher(
				String.format(
						"%s-%s-%s.%s",
						DateTimeFormatter.BASIC_ISO_DATE.format(LocalDateTime.now()),
						storeName,
						keyOfDump,
						DUMP_FILE_EXTENTION
				)).replaceAll("_");
	}

	@Override
	public void fillCache() {
	}

	@Override
	public int count() {
		return store.count();
	}

	@Override
	public Collection<VALUE> getAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void inject(Injectable injectable) {
		valueReader = injectable.injectInto(valueReader);
	}

	@Override
	public Collection<KEY> getAllKeys() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		store.clear();
	}

	@Override
	public void removeStore() {
		store.remove();
	}

	@Override
	public void close() {
		store.close();
	}

	@Data
	public static class IterationStatistic {
		private AtomicInteger totalProcessed = new AtomicInteger(0);
		private AtomicInteger failedKeys = new AtomicInteger(0);
		private AtomicInteger failedValues = new AtomicInteger(0);

		public int getTotalProcessed() {
			return totalProcessed.get();
		}

		public int getFailedKeys() {
			return failedKeys.get();
		}

		public int getFailedValues() {
			return failedValues.get();
		}

		public void incrTotalProcessed() {
			totalProcessed.incrementAndGet();
		}

		public void incrFailedKeys() {
			failedKeys.incrementAndGet();
		}

		public void incrFailedValues() {
			failedValues.incrementAndGet();
		}
	}
}
