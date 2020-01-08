package com.bakdata.conquery.integration.tests;

import com.bakdata.conquery.apiv1.FilterSearch;
import com.bakdata.conquery.apiv1.FilterTemplate;
import com.bakdata.conquery.integration.IntegrationTest;
import com.bakdata.conquery.integration.json.ConqueryTestSpec;
import com.bakdata.conquery.integration.json.JsonIntegrationTest;
import com.bakdata.conquery.models.api.description.FEValue;
import com.bakdata.conquery.models.concepts.filters.specific.AbstractSelectFilter;
import com.bakdata.conquery.models.concepts.virtual.VirtualConcept;
import com.bakdata.conquery.models.concepts.virtual.VirtualConceptConnector;
import com.bakdata.conquery.models.exceptions.ValidatorHelper;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.resources.api.ConceptsProcessor;
import com.bakdata.conquery.resources.api.ConceptsProcessor.ResolvedConceptsResult;
import com.bakdata.conquery.util.support.StandaloneSupport;
import com.github.powerlibraries.io.In;
import com.github.powerlibraries.io.Out;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FilterResolutionPrefixTest implements ProgrammaticIntegrationTest, IntegrationTest.Simple {

	private String[] lines = new String[]{
			"HEADER",
			"a",
			"aab",
			"aaa",
			"b"
	};

	@Override
	public void execute(StandaloneSupport conquery) throws Exception {
		//read test sepcification
		String testJson = In.resource("/tests/query/MULTI_SELECT_DATE_RESTRICTION_OR_CONCEPT_QUERY/MULTI_SELECT_DATE_RESTRICTION_OR_CONCEPT_QUERY.test.json").withUTF8().readAll();
		
		DatasetId dataset = conquery.getDataset().getId();
		
		ConqueryTestSpec test = JsonIntegrationTest.readJson(dataset, testJson);

		ValidatorHelper.failOnError(log, conquery.getValidator().validate(test));
		
		test.importRequiredData(conquery);
		FilterSearch
			.init(conquery.getNamespace().getNamespaces(), Collections.singleton(conquery.getNamespace().getDataset()))
			.awaitTermination(1, TimeUnit.MINUTES);

		
		VirtualConcept concept = (VirtualConcept) conquery.getNamespace().getStorage().getAllConcepts().iterator().next();
		VirtualConceptConnector connector = concept.getConnectors().iterator().next();
		AbstractSelectFilter<?> filter = (AbstractSelectFilter<?>) connector.getFilter();

		// Copy search csv from resources to tmp folder.
		final Path tmpCSv = Files.createTempFile("conquery_search", "csv");
		Out.file(tmpCSv.toFile()).writeLines(lines);

		filter.setSearchType(FilterSearch.FilterSearchType.PREFIX);
		filter.setTemplate(new FilterTemplate(tmpCSv.toString(), Arrays.asList("HEADER"), "HEADER", "", ""));

		FilterSearch.createSourceSearch(filter);

		assertThat(filter.getSourceSearch()).isNotNull();

		ConceptsProcessor processor = new ConceptsProcessor(conquery.getNamespace().getNamespaces());

		// from csv
		{
			ResolvedConceptsResult resolved = processor.resolveFilterValues(filter, List.of("a", "unknown"));

			//check the resolved values
			assertThat(resolved.getResolvedFilter().getValue().stream().map(FEValue::getValue)).containsExactlyInAnyOrder("a", "aaa", "aab");
			assertThat(resolved.getUnknownCodes()).containsExactlyInAnyOrder("unknown");
		}

		// from column values
		{
			ResolvedConceptsResult resolved = processor.resolveFilterValues(filter, List.of("f", "unknown"));

			//check the resolved values
			assertThat(resolved.getResolvedFilter().getValue().stream().map(FEValue::getValue)).containsExactlyInAnyOrder("f");
			assertThat(resolved.getUnknownCodes()).containsExactlyInAnyOrder("unknown");
		}
	}
}