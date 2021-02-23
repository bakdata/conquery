package com.bakdata.conquery.models.auth.oidc.passwordflow;

import com.bakdata.conquery.models.auth.basic.UsernamePasswordChecker;
import com.bakdata.conquery.models.auth.oidc.IntrospectionDelegatingRealmFactory;
import com.bakdata.conquery.models.auth.oidc.OIDCAuthenticationConfig;
import com.bakdata.conquery.resources.unprotected.AuthServlet.AuthAdminUnprotectedResourceProvider;
import com.bakdata.conquery.resources.unprotected.AuthServlet.AuthApiUnprotectedResourceProvider;
import com.bakdata.conquery.resources.unprotected.LoginResource;
import com.bakdata.conquery.resources.unprotected.TokenResource;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import io.dropwizard.jersey.DropwizardResourceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;


@Slf4j
@Getter
@Setter
public class TokenProcessor<C extends OIDCAuthenticationConfig> implements AuthApiUnprotectedResourceProvider, AuthAdminUnprotectedResourceProvider, UsernamePasswordChecker {

	private static final String GROUPS_CLAIM = "groups";

	private final IntrospectionDelegatingRealmFactory authProviderConf;


	public TokenProcessor(IntrospectionDelegatingRealmFactory authProviderConf) {
		this.authProviderConf = authProviderConf;
	}

	

	@Override
	public void registerAdminUnprotectedAuthenticationResources(DropwizardResourceConfig jerseyConfig) {
		jerseyConfig.register(new TokenResource(this));
		jerseyConfig.register(LoginResource.class);
	}

	@Override
	public void registerApiUnprotectedAuthenticationResources(DropwizardResourceConfig jerseyConfig) {
		jerseyConfig.register(new TokenResource(this));
	}

	@Override
	@SneakyThrows
	public String checkCredentialsAndCreateJWT(String username, char[] password) {
		
		Secret passwordSecret = new Secret(new String(password));

		AuthorizationGrant  grant = new ResourceOwnerPasswordCredentialsGrant(username, passwordSecret);
		
		URI tokenEndpoint =  UriBuilder.fromUri(authProviderConf.getTokenEndpoint()).build();

		TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, authProviderConf.getClientAuthentication(), grant, Scope.parse("openid"));
		
		
		TokenResponse response = TokenResponse.parse(tokenRequest.toHTTPRequest().send());

		if (!response.indicatesSuccess()) {
			HTTPResponse httpResponse = response.toHTTPResponse();
			log.error("Received the following error from the auth server while validating username and password:\n\tPath: {}\n\tStatus code: {}\n\tStatus message: {}\n\tContent: {}", tokenEndpoint, httpResponse.getStatusCode(), httpResponse.getStatusMessage(), httpResponse.getContent());
			throw new IllegalStateException("Unable to retrieve access token from auth server.");
		}
		else if (!(response instanceof AccessTokenResponse)) {
			log.error("Unknown token response {}.", response.getClass().getName());
			throw new IllegalStateException("Unknown token response. See log.");
		}

		AccessTokenResponse successResponse = (AccessTokenResponse) response;

		// Get the access token, the server may also return a refresh token
		AccessToken accessToken = successResponse.getTokens().getAccessToken();
		//RefreshToken refreshToken = successResponse.getTokens().getRefreshToken();
		return accessToken.getValue();
	}

}
