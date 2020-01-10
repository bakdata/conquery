package com.bakdata.conquery.resources.admin.rest;

import static com.bakdata.conquery.resources.ResourceConstants.DATASET_NAME;
import static com.bakdata.conquery.resources.ResourceConstants.TABLE_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.io.jersey.ExtraMimeTypes;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.StructureNode;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.exceptions.ConfigurationException;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.messages.namespaces.specific.UpdateDataset;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.models.worker.WorkerInformation;
import com.bakdata.conquery.resources.hierarchies.HAdmin;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Produces({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})
@Consumes({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})
@Getter
@Setter
@Path("datasets/{" + DATASET_NAME + "}")
public class AdminDatasetResource extends HAdmin {

	@PathParam(DATASET_NAME)
	protected DatasetId datasetId;
	protected Namespace namespace;

	@PostConstruct
	@Override
	public void init() {
		super.init();
		this.namespace = processor.getNamespaces().get(datasetId);
		if (namespace == null) {
			throw new WebApplicationException("Could not find dataset " + datasetId, Status.NOT_FOUND);
		}
	}

	@POST
	@Consumes(MediaType.WILDCARD)
	@Path("mapping")
	public void setIdMapping(@FormDataParam("data_csv") InputStream data) throws IOException, JSONException {
		processor.setIdMapping(data, namespace);
	}

	@POST
	@Path("label")
	public void setlabel(String label) throws IOException, JSONException {
		Dataset ds = namespace.getDataset();
		ds.setLabel(label);
		namespace.getStorage().updateDataset(ds);
	}

	@POST
	@Path("tables")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void addTable(@FormDataParam("table_schema") FormDataBodyPart schemas) throws IOException, JSONException {
		ObjectMapper mapper = processor.getNamespaces().injectInto(Jackson.MAPPER);
		for (BodyPart part : schemas.getParent().getBodyParts()) {
			try (InputStream is = part.getEntityAs(InputStream.class)) {
				Table t = mapper.readValue(is, Table.class);
				processor.addTable(namespace.getDataset(), t);
			}
		}
	}

	@POST
	@Path("imports")
	public void addImport(@QueryParam("file") File selectedFile) throws IOException, JSONException {

		StringJoiner errors = new StringJoiner("\n");

		if (!selectedFile.canRead()) {
			errors.add("Cannot read.");
		}

		if (!selectedFile.exists()) {
			errors.add("Does not exist.");
		}

		if (!selectedFile.isAbsolute()) {
			errors.add("Is not absolute.");
		}

		if (!selectedFile.getPath().endsWith(ConqueryConstants.EXTENSION_PREPROCESSED)) {
			errors.add(String.format("Does not end with `%s`.", ConqueryConstants.EXTENSION_PREPROCESSED));
		}

		if (errors.length() > 0) {
			throw new WebApplicationException(String.format("Invalid file (`%s`) supplied:\n%s.", selectedFile, errors.toString()), Status.BAD_REQUEST);
		}


		processor.addImport(namespace.getStorage().getDataset(), selectedFile);
	}

	@POST
	@Path("concepts")
	public void addConcept(Concept<?> concept) throws IOException, JSONException, ConfigurationException {
		processor.addConcept(namespace.getDataset(), concept);
	}

	@POST
	@Path("structure")
	public void setStructure(@NotNull @Valid StructureNode[] structure) throws JSONException {
		processor.setStructure(namespace.getDataset(), structure);
	}

	@DELETE
	@Path("tables/{" + TABLE_NAME + "}")
	public void removeTable(@PathParam(TABLE_NAME) TableId tableParam) throws IOException, JSONException {
		namespace.getDataset().getTables().remove(tableParam);
		namespace.getStorage().updateDataset(namespace.getDataset());
		for (WorkerInformation w : namespace.getWorkers()) {
			w.send(new UpdateDataset(namespace.getDataset()));
		}
	}
}
