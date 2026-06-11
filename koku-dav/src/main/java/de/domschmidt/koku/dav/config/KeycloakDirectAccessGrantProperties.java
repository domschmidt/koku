package de.domschmidt.koku.dav.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dav.keycloak")
public record KeycloakDirectAccessGrantProperties(
        String tokenUri, String clientId, String clientSecret, String scope) {}
