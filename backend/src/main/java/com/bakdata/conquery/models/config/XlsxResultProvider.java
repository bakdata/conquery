package com.bakdata.conquery.models.config;

import com.bakdata.conquery.commands.ManagerNode;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.io.result.ResultRender.ResultRendererProvider;
import com.bakdata.conquery.io.result.excel.ResultExcelProcessor;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.query.SingleTableResult;
import com.bakdata.conquery.resources.api.ResultExcelResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import lombok.Getter;
import lombok.SneakyThrows;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Getter
@CPSType(base = ResultRendererProvider.class, id = "XLSX")
public class XlsxResultProvider implements ResultRendererProvider {

	private boolean hidden = false;

	@Override
	@SneakyThrows(MalformedURLException.class)
	public Optional<URL> generateResultURL(ManagedExecution<?> exec, UriBuilder uriBuilder, boolean allProviders) {
		if (!(exec instanceof SingleTableResult)) {
			return Optional.empty();
		}

		if (hidden && !allProviders) {
			return Optional.empty();
		}

		return Optional.of(ResultExcelResource.getDownloadURL(uriBuilder, (ManagedExecution<?> & SingleTableResult) exec));
	}

	@Override
	public void registerResultResource(JerseyEnvironment environment, ManagerNode manager) {

		//inject required services
		environment.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(new ResultExcelProcessor(manager.getDatasetRegistry(), manager.getConfig())).to(ResultExcelProcessor.class);
			}
		});

		environment.register(ResultExcelResource.class);
	}
}
