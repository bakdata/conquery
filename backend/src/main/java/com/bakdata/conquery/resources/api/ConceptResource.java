package com.bakdata.conquery.resources.api;

import static com.bakdata.conquery.resources.ResourceConstants.CONCEPT_NAME;
import static com.bakdata.conquery.resources.ResourceConstants.DATASET_NAME;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.bakdata.conquery.io.jersey.ExtraMimeTypes;
import com.bakdata.conquery.models.api.description.FEList;
import com.bakdata.conquery.models.concepts.tree.TreeConcept;
import com.bakdata.conquery.resources.api.ConceptsProcessor.ResolvedConceptsResult;
import com.bakdata.conquery.resources.hierarchies.HConcepts;
import com.codahale.metrics.annotation.Metered;
import lombok.Getter;
import lombok.Setter;

@Metered
@Setter
@Produces({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})
@Consumes({ExtraMimeTypes.JSON_STRING, ExtraMimeTypes.SMILE_STRING})
@Path("datasets/{" + DATASET_NAME + "}/concepts/{" + CONCEPT_NAME + "}")
public class ConceptResource extends HConcepts {
	
	@Inject
	protected ConceptsProcessor processor;

	@GET
	public Response getNode() {
		FEList result = processor.getNode(concept);
		
		//check if browser still has this version cached
		if (request.getHeader(HttpHeaders.IF_NONE_MATCH) != null 
			&& result.getCacheId().equals(EntityTag.valueOf(request.getHeader(HttpHeaders.IF_NONE_MATCH)))
		) {
			return Response.status(HttpServletResponse.SC_NOT_MODIFIED).build();
		}
		else {
			return Response
				.ok(result)
				.tag(result.getCacheId())
				.build();
		}
	}
	
	@POST
	@Path("resolve")
	public ResolvedConceptsResult resolve(@NotNull ConceptCodeList conceptCodes) {
		List<String> codes = conceptCodes.getConcepts().stream().map(String::trim).collect(Collectors.toList());

		if(concept instanceof TreeConcept) {
			return processor.resolveConceptElements((TreeConcept)concept, codes);
		}
		else {
			throw new WebApplicationException("can only resolved elements on tree concepts", Status.BAD_REQUEST);
		}
	}
	
	@Getter
	@Setter
	public static class ConceptCodeList {
		private List<String> concepts;
	}
}
