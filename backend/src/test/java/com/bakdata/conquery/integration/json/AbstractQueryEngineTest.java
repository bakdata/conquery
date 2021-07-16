package com.bakdata.conquery.integration.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.bakdata.conquery.apiv1.query.Query;
import com.bakdata.conquery.integration.common.IntegrationUtils;
import com.bakdata.conquery.integration.common.LoadingUtil;
import com.bakdata.conquery.integration.common.ResourceFile;
import com.bakdata.conquery.io.result.CsvLineStreamRenderer;
import com.bakdata.conquery.io.result.ResultTestUtil;
import com.bakdata.conquery.io.result.ResultUtil;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.execution.ExecutionState;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.identifiable.mapping.IdMappingState;
import com.bakdata.conquery.models.query.ExecutionManager;
import com.bakdata.conquery.models.query.ManagedQuery;
import com.bakdata.conquery.models.query.PrintSettings;
import com.bakdata.conquery.models.query.resultinfo.ResultInfo;
import com.bakdata.conquery.models.query.results.EntityResult;
import com.bakdata.conquery.models.query.results.MultilineEntityResult;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import com.bakdata.conquery.util.NonPersistentStoreFactory;
import com.bakdata.conquery.util.support.StandaloneSupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.powerlibraries.io.In;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractQueryEngineTest extends ConqueryTestSpec {

	@Override
	public void overrideConfig(ConqueryConfig config) {
		config.setStorage(new NonPersistentStoreFactory());
	}

	@Override
	public void executeTest(StandaloneSupport standaloneSupport) throws IOException {
		DatasetRegistry namespaces = standaloneSupport.getNamespace().getNamespaces();

		Query query = getQuery();

		assertThat(standaloneSupport.getValidator().validate(query))
				.describedAs("Query Validation Errors")
				.isEmpty();


		log.info("{} QUERY INIT", getLabel());

		final ConqueryConfig config = standaloneSupport.getConfig();
		final User testUser = standaloneSupport.getTestUser();

		final ManagedExecutionId executionId = IntegrationUtils.assertQueryResult(standaloneSupport, query, -1, ExecutionState.DONE, testUser, 201);

		final ManagedQuery execution = (ManagedQuery) standaloneSupport.getMetaStorage().getExecution(executionId);

		//check result info size
		List<ResultInfo> resultInfos = execution.getResultInfo();

		assertThat(
				execution.streamResults()
						.flatMap(EntityResult::streamValues)
		)
				.as("Should have same size as result infos")
				.allSatisfy(v -> assertThat(v).hasSameSizeAs(resultInfos));

		IdMappingState mappingState = config.getIdMapping().initToExternal(testUser, execution);
		PrintSettings
				PRINT_SETTINGS =
				new PrintSettings(
						false,
						Locale.ENGLISH,
						namespaces,
						config,
						cer -> ResultUtil.createId(standaloneSupport.getNamespace(), cer, config.getIdMapping(), mappingState),
						(columnInfo) -> columnInfo.getSelect().getId().toStringWithoutDataset()
				);

		CsvLineStreamRenderer renderer = new CsvLineStreamRenderer(config.getCsv().createWriter(), PRINT_SETTINGS);

		List<String> actual = renderer.toStream(
				config.getIdMapping().getPrintIdFields(),
				resultInfos,
				execution.streamResults()
		).collect(Collectors.toList());

		ResourceFile expectedCsv = getExpectedCsv();

		List<String> expected = In.stream(expectedCsv.stream()).readLines();

		assertThat(actual).as("Results for %s are not as expected.", this).containsExactlyInAnyOrderElementsOf(expected);
		// check that getLastResultCount returns the correct size
		if (execution.streamResults().noneMatch(MultilineEntityResult.class::isInstance)) {
			assertThat(execution.getLastResultCount()).as("Result count for %s is not as expected.", this).isEqualTo(expected.size() - 1);
		}

		log.info("INTEGRATION TEST SUCCESSFUL {} {} on {} rows", getClass().getSimpleName(), this, expected.size());
	}

	@JsonIgnore
	protected abstract Query getQuery();

	protected abstract ResourceFile getExpectedCsv();
}
