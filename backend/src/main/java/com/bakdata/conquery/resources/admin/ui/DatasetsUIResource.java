package com.bakdata.conquery.resources.admin.ui;

import static com.bakdata.conquery.resources.ResourceConstants.DATASET_NAME;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.bakdata.conquery.io.jersey.ExtraMimeTypes;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.identifiable.mapping.PersistentIdMap;
import com.bakdata.conquery.models.types.MajorTypeId;
import com.bakdata.conquery.models.types.specific.AStringType;
import com.bakdata.conquery.resources.admin.ui.model.FileView;
import com.bakdata.conquery.resources.admin.ui.model.UIView;
import com.bakdata.conquery.resources.hierarchies.HDatasets;
import com.bakdata.conquery.util.io.FileTreeReduction;

import io.dropwizard.views.View;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Produces(MediaType.TEXT_HTML)
@Consumes({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})
@Getter @Setter
@Path("datasets/{" + DATASET_NAME + "}")
public class DatasetsUIResource extends HDatasets {
	
	@GET
	public View getDataset() {
		return new FileView<>(
			"dataset.html.ftl",
			processor.getUIContext(),
			new DatasetInfos(
				namespace.getDataset(),
				namespace.getStorage().getAllConcepts(),
				//total size of dictionaries
				namespace.getStorage()
					.getAllImports()
					.stream()
					.flatMap(i->Arrays.stream(i.getColumns()))
					.filter(c->c.getType().getTypeId()==MajorTypeId.STRING)
					.map(c->(AStringType)c.getType())
					.filter(c->c.getUnderlyingDictionary() != null)
					.collect(Collectors.groupingBy(t->t.getUnderlyingDictionary().getId()))
					.values()
					.stream()
					.mapToLong(l->l.get(0).estimateTypeSize())
					.sum(),
				//total size of entries
				namespace.getStorage()
					.getAllImports()
					.stream()
					.mapToLong(Import::estimateMemoryConsumption)
					.sum()
			),
			FileTreeReduction.reduceByExtension(processor.getConfig().getStorage().getPreprocessedRoot(), ".cqpp")
		);
	}
	
	@Data @AllArgsConstructor
	public static class DatasetInfos {
		private Dataset ds;
		private Collection<Concept> concepts;
		private long dictionariesSize;
		private long size;
	}
	
	@GET
	@Path("mapping")
	public View getIdMapping() {
		PersistentIdMap mapping = namespace.getStorage().getIdMapping();
		if (mapping != null && mapping.getCsvIdToExternalIdMap() != null) {
			return new UIView<>(
				"idmapping.html.ftl",
				processor.getUIContext(),
				mapping.getCsvIdToExternalIdMap()
			);
		} else {
			return new UIView<>(
				"add_idmapping.html.ftl",
				processor.getUIContext(),
				namespace.getDataset().getId()
			);
		}
	}
}