package com.bakdata.conquery.integration.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bakdata.conquery.commands.SlaveCommand;
import com.bakdata.conquery.integration.ConqueryTestSpec;
import com.bakdata.conquery.integration.IntegrationTest;
import com.bakdata.conquery.models.common.CDateRange;
import com.bakdata.conquery.models.concepts.tree.TreeConcept;
import com.bakdata.conquery.models.exceptions.ValidatorHelper;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.jobs.UpdateMatchingStats;
import com.bakdata.conquery.util.support.StandaloneSupport;
import com.bakdata.conquery.util.support.TestConquery;
import com.github.powerlibraries.io.In;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataCollectionTest {

	@RegisterExtension
	public static final TestConquery CONQUERY = new TestConquery();

	@BeforeAll
	public static void init() {
		IntegrationTest.reduceLogging();
	}
	
	@Test
	public void testRestartingDatabase() throws Exception {
		//read test sepcification
		String testJson = In.file("tests/query/SIMPLE_TREECONCEPT_QUERY/SIMPLE_TREECONCEPT_Query.test.json").withUTF8().readAll();
		try(StandaloneSupport conquery = CONQUERY.getSupport()) {
			DatasetId dataset = conquery.getDataset().getId();
			
			ConqueryTestSpec test = IntegrationTest.readTest(dataset, testJson);
			ValidatorHelper.failOnError(log, conquery.getValidator().validate(test));
			
			test.importRequiredData(conquery);
			
			//ensure the metadata is collected
			for(SlaveCommand slave : conquery.getStandaloneCommand().getSlaves()) {
				slave.getJobManager().addSlowJob(new UpdateMatchingStats(slave.getWorkers()));
			}
			
			conquery.waitUntilWorkDone();
			
			TreeConcept concept = (TreeConcept) conquery.getNamespace().getStorage().getAllConcepts().iterator().next();
			
			//check the number of matched events
			assertThat(concept.getMatchingStats().countEvents()).isEqualTo(4);
			assertThat(concept.getChildren()).allSatisfy(c -> {
				assertThat(c.getMatchingStats().countEvents()).isEqualTo(2);
			});
			
			//check the date ranges
			assertThat(concept.getMatchingStats().spanEvents())
				.isEqualTo(CDateRange.of(LocalDate.parse("2010-07-15"), LocalDate.parse("2013-11-10")));
			assertThat(concept.getChildren().get(0).getMatchingStats().spanEvents())
				.isEqualTo(CDateRange.of(LocalDate.parse("2012-01-01"), LocalDate.parse("2013-11-10")));
			assertThat(concept.getChildren().get(1).getMatchingStats().spanEvents())
				.isEqualTo(CDateRange.of(LocalDate.parse("2010-07-15"), LocalDate.parse("2012-11-11")));
		}
	}
}