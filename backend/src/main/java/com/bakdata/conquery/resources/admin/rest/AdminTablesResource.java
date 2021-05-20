package com.bakdata.conquery.resources.admin.rest;

import static com.bakdata.conquery.resources.ResourceConstants.*;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bakdata.conquery.apiv1.AdditionalMediaTypes;
import com.bakdata.conquery.io.jackson.serializer.IdParam;
import com.bakdata.conquery.io.jackson.serializer.NsIdRef;
import com.bakdata.conquery.io.jersey.ExtraMimeTypes;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.identifiable.Identifiable;
import com.bakdata.conquery.models.identifiable.ids.Id;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptId;
import com.bakdata.conquery.models.identifiable.ids.specific.ImportId;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.resources.hierarchies.HAdmin;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Produces({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})
@Consumes({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})

@Getter @Setter
@Path("datasets/{" + DATASET + "}/tables/{" + TABLE + "}")
public class AdminTablesResource extends HAdmin {

	@IdParam
	@PathParam(DATASET)
	protected Dataset dataset;

	@IdParam
	@PathParam(TABLE)
	protected Table table;

	protected Namespace namespace;

	@PostConstruct
	@Override
	public void init() {
		super.init();
		this.namespace = processor.getDatasetRegistry().get(dataset.getId());
	}

	/**
	 * Try to delete a table and all it's imports. Fails if it still has dependencies (unless force is used).
	 * @param force Force deletion of dependent concepts.
	 * @return List of dependent concepts.
	 */
	@DELETE
	public Response remove(@QueryParam("force") @DefaultValue("false") boolean force) {
		final List<ConceptId> dependents = processor.deleteTable(table, force);

		if (!force && !dependents.isEmpty()) {
			return Response.status(Status.CONFLICT)
						   .entity(dependents)
						   .build();
		}

		return Response.ok()
				.entity(dependents)
				.build();
	}

	@GET
	@Path("/imports")
	@Produces(AdditionalMediaTypes.JSON)
	public List<ImportId> listImports() {
		return namespace.getStorage()
						.getAllImports()
						.stream()
						.filter(imp -> imp.getTable().equals(table))
						.map(Import::getId)
						.collect(Collectors.toList());
	}

	@DELETE
	@Path("imports/{"+IMPORT_ID+"}")
	public void deleteImport(@NsIdRef @PathParam(IMPORT_ID) Import imp) {
		processor.deleteImport(imp);
	}




}