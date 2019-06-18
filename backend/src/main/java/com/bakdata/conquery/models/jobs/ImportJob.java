package com.bakdata.conquery.models.jobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.HCFile;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.datasets.ImportColumn;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.dictionary.DictionaryMapping;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.generation.BlockFactory;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.ImportId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.messages.namespaces.specific.AddImport;
import com.bakdata.conquery.models.messages.namespaces.specific.ImportBucket;
import com.bakdata.conquery.models.messages.namespaces.specific.UpdateDictionary;
import com.bakdata.conquery.models.messages.namespaces.specific.UpdateWorkerBucket;
import com.bakdata.conquery.models.preproc.PPColumn;
import com.bakdata.conquery.models.preproc.PPHeader;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.types.specific.StringTypeEncoded;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.models.worker.WorkerInformation;
import com.bakdata.conquery.util.RangeUtil;
import com.bakdata.conquery.util.io.SmallIn;
import com.bakdata.conquery.util.io.SmallOut;
import com.bakdata.conquery.util.progressreporter.ProgressReporter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jakewharton.byteunits.BinaryByteUnit;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ImportJob extends Job {

	private final ObjectReader headerReader = Jackson.BINARY_MAPPER.readerFor(PPHeader.class);

	private final Namespace namespace;
	private final TableId table;
	private final File importFile;

	@Override
	public void execute() throws JSONException {
		this.progressReporter.setMax(16);
		try (HCFile file = new HCFile(importFile, false)) {

			if (log.isInfoEnabled()) {
				log.info(
						"Reading HCFile {}:\n\theader size: {}\n\tcontent size: {}",
						importFile,
						BinaryByteUnit.format(file.getHeaderSize()),
						BinaryByteUnit.format(file.getContentSize())
				);
			}
			PPHeader header = readHeader(file);

			//see #161  match to table check if it exists and columns are of the right type

			//check that all workers are connected
			namespace.checkConnections();

			//update primary dictionary
			DictionaryMapping primaryMapping = createPrimaryMapping(header);
			this.progressReporter.report(1);

			//partition the new IDs between the slaves
			log.debug("\tpartition new IDs");
			for (int bucket : primaryMapping.getNewBuckets()) {
				namespace.addResponsibility(bucket);
			}
			response.dependOn(
				namespace
					.sendToAll(UpdateWorkerBucket::new)
					.awaitSuccess()
			);
			namespace.updateWorkerMap();

			//update the allIdsTable
			log.info("\tupdating id information");
			Import allIdsImp = new Import();
			allIdsImp.setName(new ImportId(table, header.getName()).toString());
			allIdsImp.setTable(new TableId(namespace.getStorage().getDataset().getId(), ConqueryConstants.ALL_IDS_TABLE));
			allIdsImp.setNumberOfEntries(header.getGroups());
			allIdsImp.setColumns(new ImportColumn[0]);
			namespace.getStorage().updateImport(allIdsImp);
			namespace.sendToAll(new AddImport(allIdsImp));
			this.progressReporter.report(1);


			//create data import and store/send it
			log.info("\tupdating import information");
			Import imp = new Import();
			imp.setName(header.getName());
			imp.setTable(table);
			imp.setNumberOfEntries(header.getRows());
			imp.setColumns(new ImportColumn[header.getColumns().length]);
			for (int i = 0; i < header.getColumns().length; i++) {
				PPColumn src = header.getColumns()[i];
				ImportColumn col = new ImportColumn();
				col.setName(src.getName());
				col.setType(src.getType());
				col.setParent(imp);
				col.setPosition(i);
				imp.getColumns()[i] = col;
			}
			namespace.getStorage().updateImport(imp);
			namespace.sendToAll(new AddImport(imp));

			this.progressReporter.report(1);
			int bucketSize = ConqueryConfig.getInstance().getCluster().getEntityBucketSize();

			//import the new ids into the all ids table
			if (primaryMapping.getNewIds() != null) {
				BlockFactory factory = allIdsImp.getBlockFactory();
				Int2ObjectMap<ImportBucket> allIdsBuckets = new Int2ObjectOpenHashMap<>(primaryMapping.getUsedBuckets().size());
				Int2ObjectMap<List<byte[]>> allIdsBytes = new Int2ObjectOpenHashMap<>(primaryMapping.getUsedBuckets().size());
				try (SmallOut buffer = new SmallOut(2048)) {
					ProgressReporter child = this.progressReporter.subJob(5);
					child.setMax(primaryMapping.getNewIds().getMax() - primaryMapping.getNewIds().getMin() + 1);

					for (int entityId : RangeUtil.iterate(primaryMapping.getNewIds())) {
						buffer.reset();
						Bucket bucket = factory.create(allIdsImp, Collections.singletonList(new Object[0]));
						bucket.writeContent(buffer);

						//copy content into ImportBucket
						int bucketNumber = Entity.getBucket(entityId, bucketSize);
						
						ImportBucket impBucket = allIdsBuckets
							.computeIfAbsent(bucketNumber, b->new ImportBucket(new BucketId(allIdsImp.getId(), b)));
						
						impBucket.getIncludedEntities().add(entityId);
						
						allIdsBytes
							.computeIfAbsent(bucketNumber, i->new ArrayList<>())
							.add(buffer.toBytes());
						
						child.report(1);
					}
				}
				sendBuckets(primaryMapping, allIdsBuckets, allIdsBytes);
			}
			
			//import the actual data
			log.info("\timporting");
			Int2ObjectMap<ImportBucket> buckets = new Int2ObjectOpenHashMap<>(primaryMapping.getUsedBuckets().size());
			Int2ObjectMap<List<byte[]>> bytes = new Int2ObjectOpenHashMap<>(primaryMapping.getUsedBuckets().size());
			ProgressReporter child = this.progressReporter.subJob(5);
			child.setMax(header.getGroups() + 1);
			try (SmallIn in = new SmallIn(file.readContent())) {
				for (long group = 0; group < header.getGroups(); group++) {
					int entityId = primaryMapping.source2Target(in.readInt(true));
					int size = in.readInt(true);
					int bucketNumber = Entity.getBucket(entityId, bucketSize);
					ImportBucket bucket = buckets
						.computeIfAbsent(bucketNumber, b->new ImportBucket(new BucketId(imp.getId(), b)));
						
					bucket.getIncludedEntities().add(entityId);
					
					bytes
						.computeIfAbsent(bucketNumber, i->new ArrayList<>())
						.add(in.readBytes(size));
					
					child.report(1);
				}
			}
			sendBuckets(primaryMapping, buckets, bytes);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load the file " + importFile, e);
		}
	}
	
	private void sendBuckets(DictionaryMapping primaryMapping, Int2ObjectMap<ImportBucket> buckets, Int2ObjectMap<List<byte[]>> bytes) {
		for(int bucketNumber : primaryMapping.getUsedBuckets()) {
			ImportBucket bucket = buckets.get(bucketNumber);
			List<byte[]> buffers = bytes.get(bucketNumber);
			bucket.setBytes(buffers.toArray(new byte[0][]));
			
			WorkerInformation responsibleWorker = namespace.getResponsibleWorkerForBucket(bucketNumber);
			if (responsibleWorker == null) {
				throw new IllegalStateException("No responsible worker for bucket " + bucketNumber);
			}
			try {
				responsibleWorker.getConnectedSlave().waitForFreeJobqueue();
			} catch (InterruptedException e) {
				log.error("Interrupted while waiting for worker " + responsibleWorker + " to have free space in queue", e);
			}
			response.dependOn(responsibleWorker.send(bucket));
		}
	}

	private DictionaryMapping createPrimaryMapping(PPHeader header) throws JSONException {
		log.debug("\tupdating primary dictionary");
		Dictionary entities = ((StringTypeEncoded)header.getPrimaryColumn().getType()).getSubType().getDictionary();
		this.progressReporter.report(1);
		log.debug("\tcompute dictionary");
		Dictionary oldPrimaryDict = namespace.getStorage().computeDictionary(ConqueryConstants.getPrimaryDictionary(namespace.getStorage().getDataset()));
		Dictionary primaryDict = Dictionary.copyUncompressed(oldPrimaryDict);
		log.debug("\tmap values");
		DictionaryMapping primaryMapping = DictionaryMapping.create(entities, primaryDict, namespace);
		
		//if no new ids we shouldn't recompress and store
		if(primaryMapping.getNewIds() == null) {
			log.debug("\t\tno new ids");
			primaryDict = oldPrimaryDict;
			this.progressReporter.report(2);
		}
		//but if there are new ids we have to
		else {
			log.debug("\t\tnew ids {}", primaryMapping.getNewIds());
			log.debug("\t\texample of new id: {}", primaryDict.getElement(primaryMapping.getNewIds().getMin()));
			log.debug("\t\tstoring");
			namespace.getStorage().updateDictionary(primaryDict);
			this.progressReporter.report(1);
			log.debug("\t\tsending");
			namespace.sendToAll(new UpdateDictionary(primaryDict));
			this.progressReporter.report(1);
		}
		
		log.debug("\tsending secondary dictionaries");
		for(PPColumn col:header.getColumns()) {
			col.getType().storeExternalInfos(namespace.getStorage(),
				(Consumer<Dictionary>)(dict -> {
					try {
						namespace.getStorage().addDictionary(dict);
						namespace.sendToAll(new UpdateDictionary(dict));
					} catch(Exception e) {
						throw new RuntimeException("Failed to store dictionary "+dict, e);
					}
				})
			);
		}
		return primaryMapping;
	}

	private PPHeader readHeader(HCFile file) throws JsonParseException, IOException {
		try (JsonParser in = Jackson.BINARY_MAPPER.getFactory().createParser(file.readHeader())) {
			PPHeader header = headerReader.readValue(in);

			log.info("Importing {} into {}", header.getName(), table);
			Table tab = namespace.getStorage().getDataset().getTables().getOrFail(table);
			if (!tab.matches(header)) {
				throw new IllegalArgumentException("The given header " + header + " does not match the table structure of " + table);
			}

			log.debug("\tparsing dictionaries");
			header.getPrimaryColumn().getType().readHeader(in);
			for (PPColumn col : header.getColumns()) {
				col.getType().readHeader(in);
			}

			header.getPrimaryColumn().getType().init(namespace.getStorage().getDataset().getId());
			for (PPColumn col : header.getColumns()) {
				col.getType().init(namespace.getStorage().getDataset().getId());
			}
			
			return header;
		}
	}

	@Override
	public String getLabel() {
		return "Importing into " + table + " from " + importFile;
	}

}
