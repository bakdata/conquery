package com.bakdata.conquery.util.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.Validator;
import javax.ws.rs.client.Client;

import com.bakdata.conquery.Conquery;
import com.bakdata.conquery.commands.ShardNode;
import com.bakdata.conquery.commands.StandaloneCommand;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.config.PreprocessingDirectories;
import com.bakdata.conquery.models.config.XodusStoreFactory;
import com.bakdata.conquery.models.execution.ExecutionState;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.messages.network.specific.RemoveWorker;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.util.Wait;
import com.bakdata.conquery.util.io.Cloner;
import com.google.common.util.concurrent.Uninterruptibles;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.testing.DropwizardTestSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Represents the test instance of Conquery.
 *
 */
@Slf4j
@RequiredArgsConstructor
public class TestConquery {

	private static final ConcurrentHashMap<String, Integer> NAME_COUNTS = new ConcurrentHashMap<>();

	@Getter
	private StandaloneCommand standaloneCommand;
	@Getter
	private DropwizardTestSupport<ConqueryConfig> dropwizard;
	private final File tmpDir;
	private final ConqueryConfig config;
	private Set<StandaloneSupport> openSupports = new HashSet<>();
	@Getter
	private Client client;

	private AtomicBoolean started = new AtomicBoolean(false);

	/**
	 * Returns the extension context used by the beforeAll-callback.
	 *
	 * @return The context.
	 */
	@Getter
	private ExtensionContext beforeAllContext;

	public synchronized StandaloneSupport openDataset(DatasetId datasetId) {
		try {
			log.info("loading dataset");

			return createSupport(datasetId, datasetId.getName());
		}
		catch (Exception e) {
			return fail("Failed to open dataset " + datasetId, e);
		}
	}

	public synchronized StandaloneSupport getSupport(String name) {
		try {
			log.info("Setting up dataset");
			int count = NAME_COUNTS.merge(name, 0, (a, b) -> a + 1);
			if (count > 0) {
				name += "[" + count + "]";
			}
			DatasetId datasetId = new DatasetId(name);
			standaloneCommand.getManager().getAdmin().getAdminProcessor().addDataset(name);
			return createSupport(datasetId, name);
		}
		catch (Exception e) {
			return fail("Failed to create a support for " + name, e);
		}
	}

	private synchronized StandaloneSupport createSupport(DatasetId datasetId, String name) {
		DatasetRegistry datasets = standaloneCommand.getManager().getDatasetRegistry();
		Namespace ns = datasets.get(datasetId);

		assertThat(datasets.getShardNodes()).hasSize(2);

		// make tmp subdir and change cfg accordingly
		File localTmpDir = new File(tmpDir, "tmp_" + name);
		localTmpDir.mkdir();
		ConqueryConfig localCfg = Cloner.clone(config, Map.of(Validator.class, standaloneCommand.getManager().getEnvironment().getValidator()));
		localCfg
			.getPreprocessor()
			.setDirectories(new PreprocessingDirectories[] { new PreprocessingDirectories(localTmpDir, localTmpDir, localTmpDir) });

		StandaloneSupport support = new StandaloneSupport(
			this,
			ns,
			ns.getStorage().getDataset(),
			localTmpDir,
			localCfg,
			standaloneCommand.getManager().getAdmin().getAdminProcessor(),
			// Getting the User from AuthorizationConfig
			standaloneCommand.getManager().getConfig().getAuthorization().getInitialUsers().get(0).getUser());

		Wait.builder().attempts(100).stepTime(50).build().until(() -> ns.getWorkers().size() == ns.getNamespaces().getShardNodes().size());

		support.waitUntilWorkDone();
		openSupports.add(support);
		return support;
	}


	@SneakyThrows
	public synchronized void shutdown(StandaloneSupport support) {
		log.info("Tearing down dataset");


		DatasetId dataset = support.getDataset().getId();

		standaloneCommand.getManager().getDatasetRegistry().get(dataset).close();
		standaloneCommand.getManager().getStorage().close();

		openSupports.remove(support);
	}

	@SneakyThrows
	public static void configurePathsAndLogging(ConqueryConfig config, File tmpDir) {

		config.setFailOnError(true);

		config.getPreprocessor().setDirectories(new PreprocessingDirectories[] { new PreprocessingDirectories(tmpDir, tmpDir, tmpDir) });
		XodusStoreFactory storageConfig = new XodusStoreFactory();
		storageConfig.setDirectory(tmpDir.toPath());
		config.setStorage(storageConfig);
		config.getStandalone().setNumberOfShardNodes(2);
		// configure logging
		config.setLoggingFactory(new TestLoggingFactory());

		config.getCluster().setEntityBucketSize(3);

	}

	@SneakyThrows
	public static void configureRandomPorts(ConqueryConfig config) {

		// set random open ports
		for (ConnectorFactory con : CollectionUtils
				.union(
						((DefaultServerFactory) config.getServerFactory()).getAdminConnectors(),
						((DefaultServerFactory) config.getServerFactory()).getApplicationConnectors())) {
			try (ServerSocket s = new ServerSocket(0)) {
				((HttpConnectorFactory) con).setPort(s.getLocalPort());
			}
		}
		try (ServerSocket s = new ServerSocket(0)) {
			config.getCluster().setPort(s.getLocalPort());
		}
	}

	public void beforeAll() throws Exception {

		log.info("Working in temporary directory {}", tmpDir);


		// define server
		dropwizard = new DropwizardTestSupport<ConqueryConfig>(TestBootstrappingConquery.class, config, app -> {
			standaloneCommand = new StandaloneCommand((Conquery) app);
			return standaloneCommand;
		});
		// start server
		dropwizard.before();

		// create HTTP client for api tests
		client = new JerseyClientBuilder(this.getDropwizard().getEnvironment())
			.withProperty(ClientProperties.CONNECT_TIMEOUT, 10000)
			.withProperty(ClientProperties.READ_TIMEOUT, 10000)
			.build("test client");
	}

	public void afterAll() throws Exception {
		client.close();
		dropwizard.after();
		FileUtils.deleteQuietly(tmpDir);
	}

	public void afterEach() throws Exception {
		for (StandaloneSupport openSupport : openSupports ) {
			log.info("Tearing down dataset");
			DatasetId dataset = openSupport.getDataset().getId();
			removeSupportDataset(openSupport);
		}
		openSupports.clear();
	}

	public void removeSupport(StandaloneSupport support) {
		openSupports.remove(support);
	}
	



	public void waitUntilWorkDone() {
		log.info("Waiting for jobs to finish");
		boolean busy;
		//sample 10 times from the job queues to make sure we are done with everything
		long started = System.nanoTime();
		for(int i=0;i<10;i++) {
			do {
				busy = standaloneCommand.getManager().getJobManager().isSlowWorkerBusy();
				busy |= standaloneCommand.getManager()
										 .getStorage()
										 .getAllExecutions()
										 .stream()
										 .map(ManagedExecution::getState)
										 .anyMatch(ExecutionState.RUNNING::equals);

				for (Namespace namespace : standaloneCommand.getManager().getDatasetRegistry().getDatasets()) {
					busy |= namespace.getJobManager().isSlowWorkerBusy();
				}

				for (ShardNode slave : standaloneCommand.getShardNodes())
					busy |= slave.isBusy();

				Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
				if(Duration.ofNanos(System.nanoTime()-started).toSeconds()>10) {
					log.warn("waiting for done work for a long time");
					started = System.nanoTime();
				}

			} while(busy);
		}
		log.info("all jobs finished");
	}

	@SneakyThrows
	public void removeSupportDataset(StandaloneSupport support) {
		DatasetId datasetId = support.getDataset().getId();
		standaloneCommand.getManager().getDatasetRegistry().removeNamespace(datasetId);
	}
}
