package de.domschmidt.koku.dav.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KeycloakDirectAccessGrantAuthenticationProviderTest {

    @Test
    void supportsUsernamePasswordAuthenticationOnly() {
        final var provider = provider(new KeycloakDirectAccessGrantProperties("uri", "client", null, null));

        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
        assertThat(provider.supports(org.springframework.security.authentication.TestingAuthenticationToken.class))
                .isFalse();
    }

    @Test
    void rejectsMissingAuthenticationAndCredentials() {
        final var provider = provider(new KeycloakDirectAccessGrantProperties("uri", "client", null, null));
        final UsernamePasswordAuthenticationToken missingUser = new UsernamePasswordAuthenticationToken("", "secret");
        final UsernamePasswordAuthenticationToken missingPassword = new UsernamePasswordAuthenticationToken("user", "");
        final UsernamePasswordAuthenticationToken nullPassword = new UsernamePasswordAuthenticationToken("user", null);

        assertThatThrownBy(() -> provider.authenticate(null)).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> provider.authenticate(missingUser)).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> provider.authenticate(missingPassword)).isInstanceOf(BadCredentialsException.class);
        assertThatThrownBy(() -> provider.authenticate(nullPassword)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsIncompleteKeycloakConfigurationBeforeNetworkAccess() {
        final var missingUri = provider(new KeycloakDirectAccessGrantProperties(null, "client", null, null));
        final var missingClient = provider(new KeycloakDirectAccessGrantProperties("uri", "", null, null));
        final var authentication = new UsernamePasswordAuthenticationToken("user", "secret");

        assertThatThrownBy(() -> missingUri.authenticate(authentication)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> missingClient.authenticate(authentication)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exchangesCredentialsAndReturnsValidatedJwtSubject() {
        final RestClient.Builder restClientBuilder = RestClient.builder();
        final MockRestServiceServer server =
                MockRestServiceServer.bindTo(restClientBuilder).build();
        final JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode("access-token"))
                .thenReturn(Jwt.withTokenValue("access-token")
                        .header("alg", "none")
                        .subject("user-id")
                        .build());
        final var provider = new KeycloakDirectAccessGrantAuthenticationProvider(
                new KeycloakDirectAccessGrantProperties("https://keycloak/token", "dav", "secret", "openid"),
                restClientBuilder.build(),
                decoder);
        server.expect(requestTo("https://keycloak/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=password")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_secret=secret")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("scope=openid")))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token\",\"token_type\":\"Bearer\",\"expires_in\":300}",
                        MediaType.APPLICATION_JSON));

        final var result = provider.authenticate(new UsernamePasswordAuthenticationToken("ada", "password"));

        assertThat(result.getName()).isEqualTo("user-id");
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        server.verify();
    }

    @Test
    void translatesRejectedCredentialsAndInvalidTokenResponses() {
        final RestClient.Builder rejectedBuilder = RestClient.builder();
        final MockRestServiceServer rejectedServer =
                MockRestServiceServer.bindTo(rejectedBuilder).build();
        rejectedServer.expect(requestTo("https://keycloak/token")).andRespond(withUnauthorizedRequest());
        final var rejectedProvider = new KeycloakDirectAccessGrantAuthenticationProvider(
                new KeycloakDirectAccessGrantProperties("https://keycloak/token", "dav", null, null),
                rejectedBuilder.build(),
                mock(JwtDecoder.class));

        final UsernamePasswordAuthenticationToken rejectedCredentials =
                new UsernamePasswordAuthenticationToken("ada", "wrong");
        assertThatThrownBy(() -> rejectedProvider.authenticate(rejectedCredentials))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid DAV credentials");
        rejectedServer.verify();

        assertTokenFailure(
                "{\"access_token\":\"\",\"token_type\":\"Bearer\",\"expires_in\":0}",
                mock(JwtDecoder.class),
                "did not contain an access token");
        final JwtDecoder badDecoder = mock(JwtDecoder.class);
        when(badDecoder.decode("access-token")).thenThrow(new BadJwtException("invalid"));
        assertTokenFailure("{\"access_token\":\"access-token\",\"expires_in\":1}", badDecoder, "Unable to validate");
        final JwtDecoder noSubjectDecoder = mock(JwtDecoder.class);
        when(noSubjectDecoder.decode("access-token"))
                .thenReturn(Jwt.withTokenValue("access-token")
                        .header("alg", "none")
                        .claim("sub", " ")
                        .build());
        assertTokenFailure(
                "{\"access_token\":\"access-token\",\"expires_in\":1}", noSubjectDecoder, "did not contain a subject");
    }

    private static void assertTokenFailure(String response, JwtDecoder decoder, String message) {
        final RestClient.Builder builder = RestClient.builder();
        final MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://keycloak/token"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        final var provider = new KeycloakDirectAccessGrantAuthenticationProvider(
                new KeycloakDirectAccessGrantProperties("https://keycloak/token", "dav", null, null),
                builder.build(),
                decoder);

        final UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken("ada", "password");
        assertThatThrownBy(() -> provider.authenticate(credentials))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining(message);
        server.verify();
    }

    private static KeycloakDirectAccessGrantAuthenticationProvider provider(
            KeycloakDirectAccessGrantProperties properties) {
        return new KeycloakDirectAccessGrantAuthenticationProvider(properties, mock(JwtDecoder.class));
    }
}
