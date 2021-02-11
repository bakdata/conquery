package com.bakdata.conquery.models.config;

import com.bakdata.conquery.commands.ManagerNode;
import com.bakdata.conquery.commands.ShardNode;
import com.bakdata.conquery.io.cps.CPSBase;
import com.bakdata.conquery.io.xodus.MetaStorage;
import com.bakdata.conquery.io.xodus.NamespaceStorage;
import com.bakdata.conquery.io.xodus.WorkerStorage;
import com.bakdata.conquery.io.xodus.stores.DirectIdentifiableStore;
import com.bakdata.conquery.io.xodus.stores.IdentifiableStore;
import com.bakdata.conquery.io.xodus.stores.SingletonStore;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.StructureNode;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.datasets.SecondaryIdDescription;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.mapping.PersistentIdMap;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import com.bakdata.conquery.models.worker.WorkerInformation;
import com.bakdata.conquery.models.worker.WorkerToBucketsMap;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.Validator;
import java.util.Collection;
import java.util.List;

@CPSBase
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
public interface StorageFactory {

	default void init(ManagerNode managerNode) {};

	MetaStorage createMetaStorage(Validator validator, List<String> pathName, DatasetRegistry datasets);

	NamespaceStorage createNamespaceStorage(Validator validator, List<String> pathName);

	WorkerStorage createWorkerStorage(Validator validator, List<String> pathName);

	Collection<NamespaceStorage> loadNamespaceStorages(ManagerNode managerNode, List<String> pathName);

	Collection<WorkerStorage> loadWorkerStorages(ShardNode shardNode, List<String> pathName);

	// NamespacedStorage (Important for serdes communication between manager and shards)
	SingletonStore<Dataset> createDatasetStore(List<String> pathName);
	IdentifiableStore<SecondaryIdDescription> createSecondaryIdDescriptionStore(CentralRegistry centralRegistry, List<String> pathName);
	IdentifiableStore<Table> createTableStore(CentralRegistry centralRegistry, List<String> pathName);
	IdentifiableStore<Dictionary> createDictionaryStore(CentralRegistry centralRegistry, List<String> pathName);
	IdentifiableStore<Concept<?>> createConceptStore(CentralRegistry centralRegistry, List<String> pathName);
	IdentifiableStore<Import> createImportStore(CentralRegistry centralRegistry, List<String> pathName);

	// WorkerStorage
	IdentifiableStore<CBlock> createCBlockStore(CentralRegistry centralRegistry, List<String> pathName);
	IdentifiableStore<Bucket> createBucketStore(CentralRegistry centralRegistry, List<String> pathName);
	SingletonStore<WorkerInformation> createWorkerInformationStore(List<String> pathName);

	// NamespaceStorage
	SingletonStore<PersistentIdMap> createIdMappingStore(List<String> pathName);
	SingletonStore<WorkerToBucketsMap> createWorkerToBucketsStore(List<String> pathName);
	SingletonStore<StructureNode[]> createStructureStore(List<String> pathName);

}