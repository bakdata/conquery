package com.bakdata.conquery.integration;

import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.util.io.ConqueryMDC;
import com.bakdata.conquery.util.support.StandaloneSupport;
import com.bakdata.conquery.util.support.TestConquery;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.function.Executable;

import java.util.HashMap;

public interface IntegrationTest {

	void execute(String name, TestConquery testConquery) throws Exception;



	public default void overrideConfig(ConqueryConfig conf){

	}

	abstract class Simple implements IntegrationTest {
		public abstract void execute(StandaloneSupport conquery) throws Exception;
		
		@Override
		public void execute(String name, TestConquery testConquery) throws Exception {
			try(StandaloneSupport conquery = testConquery.getSupport(name)) {
				// Because Shiro works with a static Security manager
				testConquery.getStandaloneCommand().getManager().getAuthController().registerShiro();

				execute(conquery);
			}
		}
	}
	
	@Slf4j
	@RequiredArgsConstructor
	final class Wrapper implements Executable {

		private final String name;
		private final TestConquery testConquery;
		private final IntegrationTest test;
		
		@Override
		public void execute() throws Throwable {

			ConqueryMDC.setLocation(name);
			log.info("STARTING integration test {}", name);
			try {
				test.execute(name, testConquery);
			}
			catch(Exception e) {
				ConqueryMDC.setLocation(name);
				log.info("FAILED integration test "+name, e);
				throw e;
			}
			ConqueryMDC.setLocation(name);
			log.info("SUCCESS integration test {}", name);
		}
	}
}
