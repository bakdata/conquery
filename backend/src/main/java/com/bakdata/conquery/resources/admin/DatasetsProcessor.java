package com.bakdata.conquery.resources.admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.HCFile;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.io.xodus.NamespaceStorage;
import com.bakdata.conquery.io.xodus.NamespaceStorageImpl;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.Connector;
import com.bakdata.conquery.models.concepts.filters.specific.ValidityDateSelectionFilter;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.exceptions.ConfigurationException;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.i18n.I18n;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.jobs.ImportJob;
import com.bakdata.conquery.models.jobs.JobManager;
import com.bakdata.conquery.models.jobs.SimpleJob;
import com.bakdata.conquery.models.messages.namespaces.specific.UpdateConcept;
import com.bakdata.conquery.models.messages.namespaces.specific.UpdateDataset;
import com.bakdata.conquery.models.messages.network.specific.AddWorker;
import com.bakdata.conquery.models.preproc.PPHeader;
import com.bakdata.conquery.models.types.MajorTypeId;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.models.worker.Namespaces;
import com.bakdata.conquery.models.worker.SlaveInformation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter @Slf4j @RequiredArgsConstructor
public class DatasetsProcessor {
	private final ConqueryConfig config;
	private final MasterMetaStorage storage;
	private final Namespaces namespaces;
	private final JobManager jobManager;
	private final ScheduledExecutorService maintenanceService;

	public void addTable(Dataset dataset, Table table) throws JSONException {
		Objects.requireNonNull(dataset);
		Objects.requireNonNull(table);
		if(table.getDataset() == null)
			table.setDataset(dataset);
		else if(!table.getDataset().equals(dataset))
			throw new IllegalArgumentException();
		
		for(int p=0;p<table.getColumns().length;p++) {
			table.getColumns()[p].setPosition(p);
		}
		table.getPrimaryColumn().setPosition(Column.PRIMARY_POSITION);
		dataset.getTables().add(table);
		namespaces.get(dataset.getId()).getStorage().updateDataset(dataset);
		namespaces.get(dataset.getId()).sendToAll(new UpdateDataset(dataset));
		//see #143  check duplicate names
	}


	public void addConcept(Dataset dataset, Concept<?> c) throws JSONException, ConfigurationException {
		
		//if there are multiple selectable dates we need to add the select date filter
		for(Connector con:c.getConnectors()) {
			if(con.getValidityDates().size()>1) {
				ValidityDateSelectionFilter f = new ValidityDateSelectionFilter();
				f.setConnector(con);
				f.setName(ConqueryConstants.VALIDITY_DATE_SELECTION_FILTER_NAME);
				f.setLabel(I18n.LABELS.getDateSelection());
				con.setDateSelectionFilter(f);
				//remove the sometimes already calculated all filters map so it is recalculated
				con.setAllFilters(null);
			}
		}
		
		c.setDataset(dataset.getId());
		jobManager.addSlowJob(new SimpleJob("Adding concept "+c.getId(), ()->namespaces.get(dataset.getId()).getStorage().updateConcept(c)));

		namespaces.get(dataset.getId()).sendToAll(new UpdateConcept(c));
		//see #144  check duplicate names
	}

	public void addDataset(String name, ScheduledExecutorService maintenanceService) throws JSONException {
		//create dataset
		Dataset dataset = new Dataset();
		dataset.setName(name);
		
		//add allIds table
		Table allIdsTable = new Table();
		{
			allIdsTable.setName(ConqueryConstants.ALL_IDS_TABLE);
			allIdsTable.setDataset(dataset);
			Column primaryColumn = new Column();
			{
				primaryColumn.setName(ConqueryConstants.ALL_IDS_TABLE___ID);
				primaryColumn.setPosition(0);
				primaryColumn.setTable(allIdsTable);
				primaryColumn.setType(MajorTypeId.STRING);
			}
			allIdsTable.setPrimaryColumn(primaryColumn);
		}
		dataset.getTables().add(allIdsTable);
		
		//store dataset in own storage
		NamespaceStorage datasetStorage = new NamespaceStorageImpl(storage.getValidator(), config.getStorage(), new File(storage.getDirectory().getParentFile(), "dataset_"+name));
		datasetStorage.setMetaStorage(storage);
		Namespace ns = new Namespace(config.getCluster().getEntityBucketSize(), datasetStorage);
		ns.initMaintenance(maintenanceService);
		ns.getStorage().updateDataset(dataset);
		namespaces.add(ns);
		
		//for now we just add one worker to every slave
		for(SlaveInformation slave : namespaces.getSlaves().values()) {
			this.addWorker(slave, dataset);
		}
	}

	public void addImport(Dataset dataset, File selectedFile) throws IOException, JSONException {
		try(HCFile hcFile = new HCFile(selectedFile, false);
				InputStream in = hcFile.readHeader()) {
			PPHeader header = Jackson.BINARY_MAPPER.readValue(in, PPHeader.class);
			
			TableId tableName = new TableId(dataset.getId(), header.getTable());
			Table table = dataset.getTables().getOrFail(tableName);
			
			
			table.getTags().add(header.getName());
			namespaces.get(dataset.getId()).getStorage().updateDataset(dataset);
			
			log.info("Importing {}", selectedFile.getAbsolutePath());
			jobManager.addSlowJob(new ImportJob(namespaces.get(dataset.getId()), table.getId(), selectedFile));
		}
	}

	public void addWorker(SlaveInformation slave, Dataset dataset) {
		slave.send(new AddWorker(dataset));
	}
}
