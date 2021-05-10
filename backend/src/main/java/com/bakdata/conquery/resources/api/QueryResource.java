package com.bakdata.conquery.resources.api;


import static com.bakdata.conquery.resources.ResourceConstants.DATASET;
import static com.bakdata.conquery.resources.ResourceConstants.QUERY;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bakdata.conquery.apiv1.AdditionalMediaTypes;
import com.bakdata.conquery.apiv1.QueryDescription;
import com.bakdata.conquery.apiv1.QueryProcessor;
import com.bakdata.conquery.apiv1.RequestAwareUriBuilder;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.execution.FullExecutionStatus;
import com.bakdata.conquery.models.execution.ManagedExecution;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@Path("datasets/{" + DATASET + "}/queries")
@Consumes(AdditionalMediaTypes.JSON)
@Produces(AdditionalMediaTypes.JSON)
@Slf4j
@Api(tags = "api")
public class QueryResource {

	private final QueryProcessor processor;

	@Inject
	public QueryResource(QueryProcessor processor) {
		this.processor = processor;
	}

	@POST
	public Response postQuery(@Auth User user, @PathParam(DATASET) Dataset dataset, @NotNull @Valid QueryDescription query, @Context HttpServletRequest req) {
		log.info("Query posted on dataset {} by user {} ({}).", dataset.getId(), user.getId(), user.getName());

		return Response.ok(
				processor.postQuery(
						dataset,
						query,
						RequestAwareUriBuilder.fromRequest(req),
						user
				))
					   .status(Status.CREATED)
					   .build();
	}

	@DELETE
	@Path("{" + QUERY + "}")
	public FullExecutionStatus cancel(@Auth User user, @PathParam(DATASET) Dataset dataset, @PathParam(QUERY) ManagedExecution<?> query, @Context HttpServletRequest req) {

		return processor.cancel(
				user,
				dataset,
				query,
				RequestAwareUriBuilder.fromRequest(req)
		);
	}

	@GET
	@Path("{" + QUERY + "}")
	public FullExecutionStatus getStatus(@Auth User user, @PathParam(DATASET) Dataset dataset, @PathParam(QUERY) ManagedExecution<?> query, @Context HttpServletRequest req)
			throws InterruptedException {

		query.awaitDone(10, TimeUnit.SECONDS);

		return processor.getStatus(
				query,
				RequestAwareUriBuilder.fromRequest(req),
				user
		);
	}
}
