package com.bakdata.conquery.commands;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import javax.validation.Validator;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.bakdata.conquery.io.mina.BinaryJacksonCoder;
import com.bakdata.conquery.io.mina.CQProtocolCodecFilter;
import com.bakdata.conquery.io.mina.ChunkReader;
import com.bakdata.conquery.io.mina.ChunkWriter;
import com.bakdata.conquery.io.mina.MinaAttributes;
import com.bakdata.conquery.io.mina.NetworkSession;
import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.io.xodus.MasterMetaStorageImpl;
import com.bakdata.conquery.io.xodus.NamespaceStorage;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.jobs.JobManager;
import com.bakdata.conquery.models.jobs.ReactingJob;
import com.bakdata.conquery.models.messages.SlowMessage;
import com.bakdata.conquery.models.messages.network.MasterMessage;
import com.bakdata.conquery.models.messages.network.NetworkMessageContext;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.models.worker.Namespaces;
import com.bakdata.conquery.models.worker.SlaveInformation;
import com.bakdata.conquery.resources.admin.AdminUIServlet;
import com.bakdata.conquery.resources.admin.ShutdownTask;
import com.bakdata.conquery.util.io.ConqueryMDC;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Getter
public class MasterCommand extends IoHandlerAdapter implements Managed {

	private IoAcceptor acceptor;
	private MasterMetaStorage storage;
	private JobManager jobManager;
	private Validator validator;
	private ConqueryConfig config;
	private AdminUIServlet admin;
	private ScheduledExecutorService maintenanceService;
	private Namespaces namespaces = new Namespaces();
	private Environment environment;

	public void run(ConqueryConfig config, Environment environment) throws IOException, JSONException {
		this.jobManager = new JobManager("master");
		this.environment = environment;
		
		RESTServer.configure(config, environment.jersey().getResourceConfig());
		
		environment.lifecycle().manage(jobManager);
		
		validator = environment.getValidator();
		this.config = config;
		
		maintenanceService = environment
			.lifecycle()
			.scheduledExecutorService("Maintenance Service")
			.build();
		
		log.info("Started meta storage");
		
		environment.lifecycle().manage(this);
		
		for(File directory : config.getStorage().getDirectory().listFiles()) {
			if(directory.getName().startsWith("dataset_")) {
				NamespaceStorage datasetStorage = NamespaceStorage.tryLoad(validator, config.getStorage(), directory);
				if(datasetStorage != null) {
					Namespace ns = new Namespace(config.getCluster().getEntityBucketSize(), datasetStorage);
					ns.initMaintenance(maintenanceService);
					namespaces.add(ns);
				}
			}
		}
		
		storage = new MasterMetaStorageImpl(environment.getValidator(), config.getStorage());
		for(Namespace sn : namespaces.getNamespaces()) {
			sn.getStorage().setMetaStorage(storage);
		}
		
		admin = new AdminUIServlet();
		admin.register(this);
		
		ShutdownTask shutdown = new ShutdownTask();
		environment.admin().addTask(shutdown);
		environment.lifecycle().addServerLifecycleListener(shutdown);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		ConqueryMDC.setLocation("Master["+session.getLocalAddress().toString()+"]");
		namespaces.getSlaves().put(session.getRemoteAddress(), new SlaveInformation(new NetworkSession(session)));
		log.info("New client {} connected, waiting for identity", session.getRemoteAddress());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		ConqueryMDC.setLocation("Master["+session.getLocalAddress().toString()+"]");
		log.info("Client '{}' disconnected ", session.getAttribute(MinaAttributes.IDENTIFIER));
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		ConqueryMDC.setLocation("Master["+session.getLocalAddress().toString()+"]");
		log.error("cought exception", cause);
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		ConqueryMDC.setLocation("Master["+session.getLocalAddress().toString()+"]");
		if(message instanceof MasterMessage) {
			MasterMessage mrm = (MasterMessage) message;
			log.trace("Master recieved {} from {}", message.getClass().getSimpleName(), session.getRemoteAddress());
			ReactingJob<MasterMessage, NetworkMessageContext.Master> job = new ReactingJob<>(mrm, new NetworkMessageContext.Master (
				jobManager,
				new NetworkSession(session),
				namespaces
			));
			
			if(mrm.isSlowMessage()) {
				((SlowMessage) mrm).setProgressReporter(job.getProgressReporter());
				jobManager.addSlowJob(job);
			}
			else {
				jobManager.addFastJob(job);
			}
		}
		else {
			log.error("Unknown message type {} in {}", message.getClass(), message);
			return;
		}
	}

	@Override
	public void start() throws Exception {
		acceptor = new NioSocketAcceptor();
		BinaryJacksonCoder coder = new BinaryJacksonCoder(namespaces, validator);
		acceptor.getFilterChain().addLast("codec", new CQProtocolCodecFilter(new ChunkWriter(coder), new ChunkReader(coder)));
		acceptor.setHandler(this);
		acceptor.getSessionConfig().setAll(config.getCluster().getMina());
		acceptor.bind(new InetSocketAddress(config.getCluster().getPort()));
		log.info("Started master @ {}", acceptor.getLocalAddress());
	}

	@Override
	public void stop() throws Exception {
		try {
			acceptor.dispose();
		} catch(Exception e) {
			log.error(acceptor+" could not be closed", e);
		}
		for(Namespace namespace : namespaces.getNamespaces()) {
			try {
				namespace.getStorage().close();
			} catch(Exception e) {
				log.error(namespace+" could not be closed", e);
			}
			
		}
		try {
			storage.close();
		} catch(Exception e) {
			log.error(storage+" could not be closed", e);
		}
	}
}
