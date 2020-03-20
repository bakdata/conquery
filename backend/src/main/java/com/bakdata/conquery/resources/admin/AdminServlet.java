package com.bakdata.conquery.resources.admin;

import java.util.ServiceLoader;

import com.bakdata.conquery.commands.MasterCommand;
import com.bakdata.conquery.io.cps.CPSBase;
import com.bakdata.conquery.io.jersey.IdParamConverter;
import com.bakdata.conquery.io.jetty.CORSPreflightRequestFilter;
import com.bakdata.conquery.io.jetty.CORSResponseFilter;
import com.bakdata.conquery.models.auth.AuthorizationController;
import com.bakdata.conquery.models.auth.web.AuthCookieFilter;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.resources.admin.rest.AdminConceptsResource;
import com.bakdata.conquery.resources.admin.rest.AdminDatasetResource;
import com.bakdata.conquery.resources.admin.rest.AdminProcessor;
import com.bakdata.conquery.resources.admin.rest.AdminResource;
import com.bakdata.conquery.resources.admin.rest.AdminTablesResource;
import com.bakdata.conquery.resources.admin.rest.AuthOverviewResource;
import com.bakdata.conquery.resources.admin.rest.GroupResource;
import com.bakdata.conquery.resources.admin.rest.PermissionResource;
import com.bakdata.conquery.resources.admin.rest.RoleResource;
import com.bakdata.conquery.resources.admin.rest.UserResource;
import com.bakdata.conquery.resources.admin.ui.AdminUIResource;
import com.bakdata.conquery.resources.admin.ui.AuthOverviewUIResource;
import com.bakdata.conquery.resources.admin.ui.ConceptsUIResource;
import com.bakdata.conquery.resources.admin.ui.DatasetsUIResource;
import com.bakdata.conquery.resources.admin.ui.GroupUIResource;
import com.bakdata.conquery.resources.admin.ui.RoleUIResource;
import com.bakdata.conquery.resources.admin.ui.TablesUIResource;
import com.bakdata.conquery.resources.admin.ui.UserUIResource;
import com.bakdata.conquery.util.ServletUtils;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.realm.Realm;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * Organizational class to provide a single implementation point for configuring
 * the admin servlet container and registering resources for it.
 */
@Getter
@Slf4j
public class AdminServlet {

	/**
	 * Marker interface for classes that provide admin UI functionality.
	 */
	@CPSBase
	public interface AuthAdminResourceProvider {
		void registerAuthenticationAdminResources(DropwizardResourceConfig jerseyConfig);
	}

	private AdminProcessor adminProcessor;
	private DropwizardResourceConfig jerseyConfig;

	public void register(MasterCommand masterCommand, AuthorizationController controller, Environment environment, ConqueryConfig config) {
		jerseyConfig = ServletUtils.createServlet("admin", environment.metrics(), config, environment.admin(), environment.getObjectMapper());

		adminProcessor = new AdminProcessor(
				config,
				masterCommand.getStorage(),
				masterCommand.getNamespaces(),
				masterCommand.getJobManager(),
				masterCommand.getMaintenanceService(),
				masterCommand.getValidator()
		);

		// inject required services
		jerseyConfig.register(new AbstractBinder() {

			@Override
			protected void configure() {
				bind(adminProcessor).to(AdminProcessor.class);
			}
		});

		// register root resources
		jerseyConfig
				.register(AdminResource.class)
				.register(AdminDatasetResource.class)
				.register(AdminConceptsResource.class)
				.register(AdminTablesResource.class)
				.register(AdminUIResource.class)
				.register(RoleResource.class)
				.register(RoleUIResource.class)
				.register(UserResource.class)
				.register(UserUIResource.class)
				.register(GroupResource.class)
				.register(GroupUIResource.class)
				.register(DatasetsUIResource.class)
				.register(TablesUIResource.class)
				.register(ConceptsUIResource.class)
				.register(PermissionResource.class)
				.register(AuthOverviewUIResource.class)
				.register(AuthOverviewResource.class);

		// Scan calsspath for Admin side plugins and register them.
		for (Realm realm : controller.getRealms()) {
			if (realm instanceof AuthAdminResourceProvider) {
				((AuthAdminResourceProvider) realm).registerAuthenticationAdminResources(jerseyConfig);
			}
		}

		// register features
		jerseyConfig
				.register(new MultiPartFeature())
				.register(new ViewMessageBodyWriter(environment.metrics(), ServiceLoader.load(ViewRenderer.class)))
				.register(new CORSPreflightRequestFilter())
				.register(masterCommand.getAuthController().getAuthenticationFilter())
				.register(IdParamConverter.Provider.INSTANCE)
				.register(CORSResponseFilter.class)
				.register(AuthCookieFilter.class);
	}
}
