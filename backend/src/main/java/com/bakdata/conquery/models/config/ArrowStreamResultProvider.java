package com.bakdata.conquery.models.config;

import com.bakdata.conquery.commands.ManagerNode;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.io.result.ResultRender.ResultRendererProvider;
import com.bakdata.conquery.io.result.arrow.ResultArrowStreamProcessor;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.query.SingleTableResult;
import com.bakdata.conquery.resources.api.ResultArrowStreamResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import lombok.Getter;
import lombok.SneakyThrows;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Getter
@CPSType(base = ResultRendererProvider.class, id = "ARROW_STREAM")
public class ArrowStreamResultProvider implements ResultRendererProvider {

	private boolean hidden = true;

	@Override
	@SneakyThrows(MalformedURLException.class)
	public Optional<URL> generateResultURL(ManagedExecution<?> exec, UriBuilder uriBuilder, boolean allProviders) {
		if (!(exec instanceof SingleTableResult)) {
			return Optional.empty();
		}

		if (hidden && !allProviders) {
			return Optional.empty();
		}

		return Optional.of(ResultArrowStreamResource.getDownloadURL(uriBuilder, (ManagedExecution<?> & SingleTableResult) exec));
	}

	@Override
	public void registerResultResource(JerseyEnvironment environment, ManagerNode manager) {

		//inject required services
		environment.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(new ResultArrowStreamProcessor(manager.getDatasetRegistry(), manager.getConfig())).to(ResultArrowStreamProcessor.class);
			}
		});

		environment.register(ResultArrowStreamResource.class);
	}
}
