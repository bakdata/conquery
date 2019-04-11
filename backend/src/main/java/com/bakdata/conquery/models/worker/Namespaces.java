package com.bakdata.conquery.models.worker;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.io.xodus.NamespaceStorage;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.IdMap;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.WorkerId;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

public class Namespaces implements NamespaceCollection {

	private ConcurrentMap<DatasetId, Namespace> datasets = new ConcurrentHashMap<>();
	@NotNull
	@Getter
	@Setter
	private IdMap<WorkerId, WorkerInformation> workers = new IdMap<>();
	@Getter
	@JsonIgnore
	private transient ConcurrentMap<SocketAddress, SlaveInformation> slaves = new ConcurrentHashMap<>();
	@Getter @Setter @JsonIgnore
	private transient MasterMetaStorage metaStorage;

	public void add(Namespace ns) {
		datasets.put(ns.getStorage().getDataset().getId(), ns);
		ns.setNamespaces(this);
	}

	public Namespace get(DatasetId dataset) {
		return datasets.get(dataset);
	}

	@Override
	public CentralRegistry findRegistry(DatasetId dataset) {
		return datasets.get(dataset).getStorage().getCentralRegistry();
	}
	
	@Override
	public CentralRegistry getMetaRegistry() {
		return metaStorage.getCentralRegistry();
	}

	public synchronized void register(SlaveInformation slave, WorkerInformation info) {
		WorkerInformation old = workers.getOptional(info.getId()).orElse(null);
		if (old != null) {
			old.setIncludedBuckets(info.getIncludedBuckets());
			old.setConnectedSlave(slave);
		}
		else {
			info.setConnectedSlave(slave);
			workers.add(info);
		}

		Namespace ns = datasets.get(info.getDataset());
		if (ns == null) {
			throw new NoSuchElementException(
				"Trying to register a worker for unknown dataset '" + info.getDataset() + "'. I only know " + datasets.keySet());
		}
		ns.addWorker(info);
	}

	public List<Dataset> getAllDatasets() {
		return datasets.values().stream().map(Namespace::getStorage).map(NamespaceStorage::getDataset).collect(Collectors.toList());
	}

	public Collection<Namespace> getNamespaces() {
		return datasets.values();
	}
}
