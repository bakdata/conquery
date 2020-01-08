package com.bakdata.conquery.io.xodus;

import com.bakdata.conquery.io.xodus.stores.IdentifiableStore;
import com.bakdata.conquery.io.xodus.stores.KeyIncludingStore;
import com.bakdata.conquery.io.xodus.stores.SingletonStore;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.config.StorageConfig;
import com.bakdata.conquery.models.events.BlockManager;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.CBlockId;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptId;
import com.bakdata.conquery.models.worker.WorkerInformation;
import com.bakdata.conquery.util.functions.Collector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Validator;
import java.io.File;
import java.util.Collection;

@Slf4j
public class WorkerStorageImpl extends NamespacedStorageImpl implements WorkerStorage {

	private SingletonStore<WorkerInformation> worker;
	private IdentifiableStore<Bucket> blocks;
	private IdentifiableStore<CBlock> cBlocks;
	@Getter
	private BlockManager blockManager;
	
	public WorkerStorageImpl(Validator validator, StorageConfig config, File directory) {
		super(validator, config, directory);
	}
	
	@Override
	public void setBlockManager(BlockManager blockManager) {
		this.blockManager = blockManager;
	}

	@Override
	protected void createStores(Collector<KeyIncludingStore<?, ?>> collector) {
		super.createStores(collector);
		worker = StoreInfo.WORKER.singleton(getEnvironment(), getValidator());
		blocks = StoreInfo.BUCKETS.identifiable(getEnvironment(), getValidator(), getCentralRegistry());
		cBlocks = StoreInfo.C_BLOCKS.identifiable(getEnvironment(), getValidator(), getCentralRegistry());
		
		collector
			.collect(worker)
			.collect(blocks)
			.collect(cBlocks);
	}
	
	@Override
	public void addCBlock(CBlock cBlock) throws JSONException {
		cBlocks.add(cBlock);
	}

	@Override
	public CBlock getCBlock(CBlockId id) {
		return cBlocks.get(id);
	}

	@Override
	public void updateCBlock(CBlock cBlock) throws JSONException {
		cBlocks.update(cBlock);
	}

	@Override
	public void removeCBlock(CBlockId id) {
		cBlocks.remove(id);
	}
	
	@Override
	public Collection<CBlock> getAllCBlocks() {
		return cBlocks.getAll();
	}
	
	@Override
	public void addBucket(Bucket bucket) throws JSONException {
		blocks.add(bucket);
		if(getBlockManager()!=null) {
			getBlockManager().addBucket(bucket);
		}
	}

	@Override
	public Bucket getBucket(BucketId id) {
		return blocks.get(id);
	}
	
	@Override
	public void removeBucket(BucketId id) {
		blocks.remove(id);
		if(getBlockManager()!=null) {
			getBlockManager().removeBucket(id);
		}
	}
	
	@Override
	public Collection<Bucket> getAllBuckets() {
		return blocks.getAll();
	}

	@Override
	public WorkerInformation getWorker() {
		return worker.get();
	}

	@Override
	public void setWorker(WorkerInformation worker) throws JSONException {
		this.worker.add(worker);
	}

	@Override
	public void updateWorker(WorkerInformation worker) throws JSONException {
		this.worker.update(worker);
	}
	
	//block manager overrides
	@Override
	public void updateConcept(Concept<?> concept) throws JSONException {
		concepts.update(concept);
		if(blockManager!=null) {
			blockManager.removeConcept(concept.getId());
			blockManager.addConcept(concept);
		}
	}

	@Override
	public void removeConcept(ConceptId id) {
		concepts.remove(id);
		if(blockManager!=null) {
			blockManager.removeConcept(id);
		}
	}
}
