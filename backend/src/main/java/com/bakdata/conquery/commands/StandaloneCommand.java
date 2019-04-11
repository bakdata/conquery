package com.bakdata.conquery.commands;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.bakdata.conquery.Conquery;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.util.io.ConfigCloner;
import com.bakdata.conquery.util.io.ConqueryMDC;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;

@Slf4j
@Getter
public class StandaloneCommand extends io.dropwizard.cli.ServerCommand<ConqueryConfig> {

	private final Conquery conquery;
	private MasterCommand master;
	private final List<SlaveCommand> slaves = new Vector<>();

	public StandaloneCommand(Conquery conquery) {
		super(conquery, "standalone", "starts a server and a client at the same time.");
		this.conquery = conquery;
	}

	// this must be overridden so that
	@Override
	protected void run(Bootstrap<ConqueryConfig> bootstrap, Namespace namespace, ConqueryConfig configuration) throws Exception {
		final Environment environment = new Environment(
			bootstrap.getApplication().getName(),
			bootstrap.getObjectMapper(),
			bootstrap.getValidatorFactory().getValidator(),
			bootstrap.getMetricRegistry(),
			bootstrap.getClassLoader(),
			bootstrap.getHealthCheckRegistry());
		configuration.getMetricsFactory().configure(environment.lifecycle(), bootstrap.getMetricRegistry());
		configuration.getServerFactory().configure(environment);

		bootstrap.run(configuration, environment);
		startStandalone(environment, namespace, configuration);
	}

	protected void startStandalone(Environment environment, Namespace namespace, ConqueryConfig config) throws Exception {
		// start master
		ConqueryMDC.setLocation("Master");
		log.debug("Starting Master");
		ConqueryConfig masterConfig = ConfigCloner.clone(config);
		masterConfig.getStorage().setDirectory(new File(masterConfig.getStorage().getDirectory(), "master"));
		masterConfig.getStorage().getDirectory().mkdir();
		conquery.run(masterConfig, environment);
		
		//create thread pool to start multiple slaves at the same time
		ExecutorService starterPool = Executors.newCachedThreadPool(
				new ThreadFactoryBuilder()
				.setNameFormat("Slave Storage Loader %d")
				.setUncaughtExceptionHandler((t, e) -> {
					ConqueryMDC.setLocation(t.getName());
					log.error(t.getName()+" failed to init storage of slave", e);
				})
				.build());
		
		for(int i=0;i<config.getStandalone().getNumberOfSlaves();i++) {
			final int id = i;
			starterPool.submit(() -> {
				ConqueryMDC.setLocation("Slave " + id);
				ConqueryConfig clone = ConfigCloner.clone(config);
				clone.getStorage().setDirectory(new File(clone.getStorage().getDirectory(), "slave_" + id));
				clone.getStorage().getDirectory().mkdir();

				SlaveCommand sc = new SlaveCommand();
				sc.setLabel("slave " + id);
				this.slaves.add(sc);
				synchronized (environment) {
					sc.run(environment, namespace, clone);
				}
				return sc;
			});
		}
		ConqueryMDC.setLocation("Master");
		log.debug("Waiting for slaves to start");
		starterPool.shutdown();
		starterPool.awaitTermination(1, TimeUnit.HOURS);

		// starts the Jersey Server
		log.debug("Starting REST Server");
		ConqueryMDC.setLocation(null);
		super.run(environment, namespace, config);
		master = conquery.getMaster();
	}
}
