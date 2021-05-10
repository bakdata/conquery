package com.bakdata.conquery.resources.api;

import static com.bakdata.conquery.resources.ResourceConstants.DATASET;
import static com.bakdata.conquery.resources.ResourceConstants.FORM_CONFIG;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bakdata.conquery.apiv1.FormConfigPatch;
import com.bakdata.conquery.apiv1.forms.FormConfigAPI;
import com.bakdata.conquery.io.jersey.ExtraMimeTypes;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.forms.configs.FormConfig;
import com.bakdata.conquery.models.forms.configs.FormConfig.FormConfigFullRepresentation;
import com.bakdata.conquery.models.forms.configs.FormConfig.FormConfigOverviewRepresentation;
import com.bakdata.conquery.models.forms.frontendconfiguration.FormConfigProcessor;
import com.bakdata.conquery.models.forms.frontendconfiguration.FormConfigProcessor.PostResponse;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.PATCH;
import io.swagger.annotations.Api;

@Consumes(ExtraMimeTypes.JSON_STRING)
@Produces(ExtraMimeTypes.JSON_STRING)
@Path("datasets/{" + DATASET + "}/form-configs")
@Api(tags = "api")
public class FormConfigResource {
	
	@PathParam(DATASET)
	private Dataset dataset;
	@Inject
	private FormConfigProcessor processor;

	@POST
	public Response postConfig(@Auth User user, @Valid FormConfigAPI config) {
		return Response.ok(new PostResponse(processor.addConfig(user, dataset, config).getId())).status(Status.CREATED).build();
	}
	
	@GET
	public Stream<FormConfigOverviewRepresentation> getConfigByUserAndType(@Auth User user, @QueryParam("formType") Set<String> formType) {
		return processor.getConfigsByFormType(user, dataset, formType);
	}

	@GET
	@Path("{" + FORM_CONFIG + "}")
	public FormConfigFullRepresentation getConfig(@Auth User user, @PathParam(FORM_CONFIG) FormConfig form) {
		return processor.getConfig(user, form);
	}
	
	@PATCH
	@Path("{" + FORM_CONFIG + "}")
	public FormConfigFullRepresentation patchConfig(@Auth User user, @PathParam(FORM_CONFIG) FormConfig form, FormConfigPatch patch ) {
		return processor.patchConfig(user, form, patch);
	}
	
	@DELETE
	@Path("{" + FORM_CONFIG + "}")
	public Response deleteConfig(@Auth User user, @PathParam(FORM_CONFIG) FormConfig form) {
		processor.deleteConfig(user, form);
		return Response.ok().build();
	}
	
}
