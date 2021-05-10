package com.bakdata.conquery.resources.api;

import static com.bakdata.conquery.resources.ResourceConstants.*;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.bakdata.conquery.apiv1.AdditionalMediaTypes;
import com.bakdata.conquery.io.result.arrow.ResultArrowProcessor;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.execution.ManagedExecution;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("datasets/{" + DATASET + "}/result/")
@Api(tags = "api")
public class ResultArrowStreamResource {
	@Inject
	private ResultArrowProcessor processor;
	
	@GET
	@Path("{" + QUERY + "}." + FILE_EXTENTION_ARROW_STREAM)
	@Produces(AdditionalMediaTypes.ARROW_STREAM)
	public Response get(
		@Auth User user,
		@PathParam(DATASET) Dataset dataset,
		@PathParam(QUERY) ManagedExecution<?> query,
		@QueryParam("pretty") Optional<Boolean> pretty)
	{
		log.info("Result for {} download on dataset {} by user {} ({}).", query, dataset, user.getId(), user.getName());
		return processor.getArrowStreamResult(user, query, dataset, pretty.orElse(false));
	}
}
