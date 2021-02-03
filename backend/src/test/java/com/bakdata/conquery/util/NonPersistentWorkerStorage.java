package com.bakdata.conquery.util;

import com.bakdata.conquery.io.xodus.WorkerStorage;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.identifiable.IdMap;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.CBlockId;
import com.bakdata.conquery.models.worker.WorkerInformation;

import javax.validation.Validator;
import java.util.Collection;

public class NonPersistentWorkerStorage extends NonPersistentNamespacedCentralRegisteredStorage implements WorkerStorage {

    private IdMap<BucketId, Bucket> buckets = new IdMap<>();
    private IdMap<CBlockId, CBlock> cBlocks = new IdMap<>();
    private WorkerInformation workerInformation;

    public NonPersistentWorkerStorage(Validator validator) {
        super(validator);
    }

    @Override
    public String getStorageOrigin() {
        return null;
    }

    @Override
    public WorkerInformation getWorker() {
        return workerInformation;
    }

    @Override
    public void setWorker(WorkerInformation worker) {
        this.workerInformation = worker;
    }

    @Override
    public void updateWorker(WorkerInformation worker) {
        this.workerInformation = worker;
    }

    @Override
    public void addBucket(Bucket bucket) {
        buckets.add(bucket);
        bucket.loadDictionaries(this);
    }

    @Override
    public Bucket getBucket(BucketId id) {
        return buckets.get(id);
    }

    @Override
    public void removeBucket(BucketId id) {
        buckets.remove(id);
    }

    @Override
    public Collection<Bucket> getAllBuckets() {
        return buckets.values();
    }

    @Override
    public void addCBlock(CBlock cBlock) {
        cBlocks.add(cBlock);
    }

    @Override
    public CBlock getCBlock(CBlockId id) {
        return cBlocks.get(id);
    }

    @Override
    public void updateCBlock(CBlock cBlock) {
        cBlocks.update(cBlock);
    }

    @Override
    public void removeCBlock(CBlockId id) {
        cBlocks.remove(id);
    }

    @Override
    public Collection<CBlock> getAllCBlocks() {
        return cBlocks.values();
    }
}
