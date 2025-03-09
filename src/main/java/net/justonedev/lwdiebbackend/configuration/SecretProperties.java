package net.justonedev.lwdiebbackend.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class holds sensitive configuration secrets for the application,
 * mapped from the {@code dashboard.secrets} namespace.
 * <p>
 * Includes secret keys for Keycloak, KVV, and route APIs.
 */
@ConfigurationProperties("dashboard.secrets")
@Component
@Getter
@Setter
public class SecretProperties {
    private String keycloakClientSecret;
    private String keycloakClientId;

    private String kvvSecret;
    private String kvvURL;
    private String routeApiKey;
}
