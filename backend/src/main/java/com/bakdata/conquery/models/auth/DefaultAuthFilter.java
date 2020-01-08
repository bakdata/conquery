package com.bakdata.conquery.models.auth;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.realm.AuthorizingRealm;

import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.models.auth.entities.User;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.DefaultUnauthorizedHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This filter hooks into dropwizard's request handling to extract and process
 * security relevant information for protected resources. Under the hood it sets
 * up shiro's security management, for the authentication of the requests. This
 * security management is then also used for authorizations based on
 * permissions, that the handling of a request triggers.
 */
@Slf4j
@PreMatching
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultAuthFilter extends AuthFilter<ConqueryToken, User> {

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {

		ConqueryToken credentials = ConquerySecurityContext.class.cast(requestContext.getSecurityContext()).getToken();

		try {
			// sets the security context in the request AND does the authentication
			if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
				throw new NotAuthorizedException("Failed to authenticate request");
			}
		}
		catch (AuthenticationException e) {
			log
				.warn(
					"Shiro failed to authenticate the request. See the following message by {}:\n\t{}",
					e.getStackTrace()[0],
					e.getMessage());
			throw new NotAuthorizedException("Failed to authenticate request. The cause has been logged.");
		}
	}

	/**
	 * Builder for {@link DefaultAuthFilter}.
	 * <p>
	 * An {@link Authenticator} must be provided during the building process.
	 * </p>
	 *
	 * @param <P>
	 *            the principal
	 */
	public static class Builder extends AuthFilterBuilder<ConqueryToken, User, DefaultAuthFilter> {

		@Override
		protected DefaultAuthFilter newInstance() {
			return new DefaultAuthFilter();
		}
	}

	public static AuthFilter<ConqueryToken, User> asDropwizardFeature(MasterMetaStorage storage, AuthConfig config) {
		AuthorizingRealm realm = config.getRealm(storage);


		Builder builder = new Builder();
		AuthFilter<ConqueryToken, User> authFilter = builder
			.setAuthenticator(new ConqueryAuthenticator(storage, realm))
			.setUnauthorizedHandler(new DefaultUnauthorizedHandler())
			.buildAuthFilter();
		return authFilter;
	}
}
