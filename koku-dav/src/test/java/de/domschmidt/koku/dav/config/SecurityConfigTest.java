package de.domschmidt.koku.dav.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.firewall.RequestRejectedException;

class SecurityConfigTest {

    @Test
    void allowsDavReadMethodsButRejectsWritesAtTheFirewall() {
        final var firewall = new SecurityConfig().httpFirewall();
        final MockHttpServletRequest report = request("REPORT");
        final MockHttpServletRequest post = request("POST");

        assertThat(firewall.getFirewalledRequest(report).getMethod()).isEqualTo("REPORT");
        assertThatThrownBy(() -> firewall.getFirewalledRequest(post)).isInstanceOf(RequestRejectedException.class);
    }

    @Test
    void discoversJwtConfigurationFromIssuerWhenNoJwkSetUriIsConfigured() {
        final NimbusJwtDecoder decoder = mock(NimbusJwtDecoder.class);
        final NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder =
                mock(NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder.class);
        try (var jwtDecoder = mockStatic(NimbusJwtDecoder.class)) {
            jwtDecoder
                    .when(() -> NimbusJwtDecoder.withIssuerLocation("https://issuer.example"))
                    .thenReturn(builder);
            when(builder.build()).thenReturn(decoder);

            assertThat(new SecurityConfig().jwtDecoder("https://issuer.example", ""))
                    .isSameAs(decoder);
        }
    }

    @Test
    void loadsJwtKeysFromConfiguredJwkSetAndStillValidatesTheIssuer() {
        final NimbusJwtDecoder decoder = mock(NimbusJwtDecoder.class);
        final NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder builder =
                mock(NimbusJwtDecoder.JwkSetUriJwtDecoderBuilder.class);
        try (var jwtDecoder = mockStatic(NimbusJwtDecoder.class)) {
            jwtDecoder
                    .when(() -> NimbusJwtDecoder.withJwkSetUri("http://idm/realms/koku/certs"))
                    .thenReturn(builder);
            when(builder.build()).thenReturn(decoder);

            assertThat(new SecurityConfig().jwtDecoder("https://issuer.example", "http://idm/realms/koku/certs"))
                    .isSameAs(decoder);
            verify(decoder).setJwtValidator(any());
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void configuresStatelessAuthenticatedBasicApi() {
        final HttpSecurity http = mock(HttpSecurity.class);
        final AuthenticationProvider provider = mock(AuthenticationProvider.class);
        final CsrfConfigurer<HttpSecurity> csrf = mock(CsrfConfigurer.class, RETURNS_SELF);
        final SessionManagementConfigurer<HttpSecurity> sessions =
                mock(SessionManagementConfigurer.class, RETURNS_SELF);
        final AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry authorization = mock(
                AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, RETURNS_DEEP_STUBS);
        final DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        when(http.csrf(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(csrf);
            return http;
        });
        when(http.authenticationProvider(provider)).thenReturn(http);
        when(http.sessionManagement(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(sessions);
            return http;
        });
        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(authorization);
            return http;
        });
        when(http.httpBasic(any())).thenReturn(http);
        when(http.build()).thenReturn(chain);

        assertThat(new SecurityConfig().filterChain(http, provider)).isSameAs(chain);

        verify(csrf).csrfTokenRepository(any());
        verify(csrf).csrfTokenRequestHandler(any());
        verify(http).authenticationProvider(provider);
        verify(sessions).sessionCreationPolicy(any());
        verify(authorization).requestMatchers("/error", "/actuator/health");
        verify(http).httpBasic(any());
    }

    @Test
    void wrapsSecurityFilterChainBuildFailures() {
        final HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        when(http.build()).thenThrow(new IllegalArgumentException("broken"));
        final SecurityConfig config = new SecurityConfig();
        final AuthenticationProvider provider = mock(AuthenticationProvider.class);

        assertThatThrownBy(() -> config.filterChain(http, provider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to build security filter chain")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    private MockHttpServletRequest request(String method) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/");
        request.setServletPath("/");
        return request;
    }
}
