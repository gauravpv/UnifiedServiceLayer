package com.bajaj.config;

import com.bajaj.constants.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class GatewayAuthConfig {

    private final AppProperties appProperties;
    private final Environment environment;

    public String ocpSubKey() {
        return firstConfigured(
                appProperties.getGateway().getOcpSubKey(),
                environment.getProperty("app.gateway.ocp-sub-key"),
                environment.getProperty("app.gateway.ocp_sub_key"),
                environment.getProperty(Constants.OCP_SUB_KEY));
    }

    public String authClientId() {
        return firstConfigured(
                appProperties.getGateway().getAuthClientId(),
                environment.getProperty("app.gateway.auth-client-id"),
                environment.getProperty("app.gateway.auth_client_id"),
                environment.getProperty(Constants.AUTH_CLIENT_ID));
    }

    public String authClientSecret() {
        return firstConfigured(
                appProperties.getGateway().getAuthClientSecret(),
                environment.getProperty("app.gateway.auth-client-secret"),
                environment.getProperty("app.gateway.auth_client_secret"),
                environment.getProperty(Constants.AUTH_CLIENT_SECRET));
    }

    public String authTokenUrl() {
        return firstConfigured(
                appProperties.getGateway().getAuthTokenUrl(),
                environment.getProperty("app.gateway.auth-token-url"),
                environment.getProperty("app.gateway.auth_token_url"),
                environment.getProperty(Constants.AUTH_TOKEN_URL));
    }

    public boolean hasOcpSubKey() {
        return StringUtils.hasText(ocpSubKey());
    }

    public boolean hasTokenCredentials() {
        return StringUtils.hasText(authTokenUrl())
                && StringUtils.hasText(authClientId())
                && StringUtils.hasText(authClientSecret());
    }

    private static String firstConfigured(String... candidates) {
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate.trim();
            }
        }
        return null;
    }
}
