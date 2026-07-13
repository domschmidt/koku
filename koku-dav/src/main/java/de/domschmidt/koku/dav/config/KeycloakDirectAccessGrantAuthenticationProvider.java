package de.domschmidt.koku.dav.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class KeycloakDirectAccessGrantAuthenticationProvider implements AuthenticationProvider {

    private final KeycloakDirectAccessGrantProperties properties;
    private final RestClient restClient;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public KeycloakDirectAccessGrantAuthenticationProvider(
            final KeycloakDirectAccessGrantProperties properties, final JwtDecoder jwtDecoder) {
        this(properties, RestClient.create(), jwtDecoder);
    }

    KeycloakDirectAccessGrantAuthenticationProvider(
            final KeycloakDirectAccessGrantProperties properties,
            final RestClient restClient,
            final JwtDecoder jwtDecoder) {
        this.properties = properties;
        this.restClient = restClient;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (authentication == null) {
            throw new BadCredentialsException("Missing DAV authentication");
        }
        final String username = authentication.getName();
        final String password = authentication.getCredentials() == null
                ? ""
                : authentication.getCredentials().toString();
        if (!hasText(username) || !hasText(password)) {
            throw new BadCredentialsException("Missing DAV credentials");
        }

        final KeycloakTokenResponse token = requestToken(username, password);
        final String subject = extractSubject(token);
        return UsernamePasswordAuthenticationToken.authenticated(
                subject, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private KeycloakTokenResponse requestToken(final String username, final String password) {
        final String tokenUri = properties.tokenUri();
        final String clientId = properties.clientId();
        if (!hasText(tokenUri) || !hasText(clientId)) {
            throw new IllegalStateException("DAV Keycloak token URI and client ID must be configured");
        }

        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);
        final String clientSecret = properties.clientSecret();
        if (hasText(clientSecret)) {
            form.add("client_secret", clientSecret);
        }
        final String scope = properties.scope();
        if (hasText(scope)) {
            form.add("scope", scope);
        }

        try {
            return restClient
                    .post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
        } catch (final RestClientResponseException e) {
            log.warn(
                    "Keycloak rejected DAV Basic Auth credentials for user '{}': HTTP {}", username, e.getStatusCode());
            throw new BadCredentialsException("Invalid DAV credentials", e);
        }
    }

    private String extractSubject(final KeycloakTokenResponse token) {
        if (token == null || !hasText(token.accessToken())) {
            throw new BadCredentialsException("Keycloak token response did not contain an access token");
        }
        try {
            final Jwt jwt = jwtDecoder.decode(token.accessToken());
            final String subject = jwt.getSubject();
            if (hasText(subject)) {
                return subject;
            }
        } catch (final BadJwtException e) {
            throw new BadCredentialsException("Unable to validate Keycloak access token", e);
        }
        throw new BadCredentialsException("Keycloak access token did not contain a subject");
    }

    private static boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    private record KeycloakTokenResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("access_token")
            String accessToken,

            @com.fasterxml.jackson.annotation.JsonProperty("token_type")
            String tokenType,

            @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
            long expiresIn) {}
}
