package com.bakdata.conquery.util;

import javax.servlet.FilterRegistration;

import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.bakdata.conquery.models.config.ConqueryConfig;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * The URL rewriting (in a Dropwizard application) to allow for use of React
 * Router's BrowserHistory. The purpose is to allow for using HTML5 URLs
 * (without the #).
 */
public class UrlRewriteBundle implements ConfiguredBundle<ConqueryConfig> {

	public static final String DEFAULT_CONF_PATH = "urlrewrite.xml";

	public UrlRewriteBundle() {}

	@Override
	public void run(ConqueryConfig configuration, Environment environment) throws Exception {
		FilterRegistration.Dynamic registration = environment.servlets().addFilter("UrlRewriteFilter", new UrlRewriteFilter());
		registration.addMappingForUrlPatterns(null, true, "/*");
		registration.setInitParameter("confPath", DEFAULT_CONF_PATH);
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
		/* nothing */
	}
}