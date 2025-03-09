package net.justonedev.lwdiebbackend.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class configures properties for application variables,
 * mapped from the {@code dashboard.variable} namespace.
 * Includes URIs and URLs for Keycloak authentication and frontend redirects.
 */
@ConfigurationProperties("dashboard.variable")
@Component
@Getter
@Setter
public class VariableProperties {
    private String keycloakRedirectUri;
    private String keycloakAuthorizationUri;
    private String keycloakTokenUri;
    private String keycloakLogoutUri;

    private String frontendUrl;
    private String frontendCallbackUrl;
    private String frontendLogoutRedirectUrl;
}
