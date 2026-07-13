package de.domschmidt.koku.user.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;

class SecurityConfigTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void configuresStatelessAuthenticatedJwtApi() throws Exception {
        final HttpSecurity http = mock(HttpSecurity.class);
        final CsrfConfigurer<HttpSecurity> csrf = mock(CsrfConfigurer.class, RETURNS_SELF);
        final SessionManagementConfigurer<HttpSecurity> sessions =
                mock(SessionManagementConfigurer.class, RETURNS_SELF);
        final ExceptionHandlingConfigurer<HttpSecurity> exceptions =
                mock(ExceptionHandlingConfigurer.class, RETURNS_SELF);
        final AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry authorization = mock(
                AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, RETURNS_DEEP_STUBS);
        final OAuth2ResourceServerConfigurer<HttpSecurity> oauth =
                mock(OAuth2ResourceServerConfigurer.class, RETURNS_SELF);
        final OAuth2ResourceServerConfigurer.JwtConfigurer jwt =
                mock(OAuth2ResourceServerConfigurer.JwtConfigurer.class, RETURNS_SELF);
        final DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        when(http.csrf(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(csrf);
            return http;
        });
        when(http.sessionManagement(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(sessions);
            return http;
        });
        when(http.exceptionHandling(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(exceptions);
            return http;
        });
        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(authorization);
            return http;
        });
        when(oauth.jwt(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(jwt);
            return oauth;
        });
        when(http.oauth2ResourceServer(any())).thenAnswer(invocation -> {
            ((Customizer) invocation.getArgument(0)).customize(oauth);
            return http;
        });
        when(http.build()).thenReturn(chain);

        assertThat(new SecurityConfig().filterChain(http)).isSameAs(chain);

        verify(csrf).csrfTokenRepository(any());
        verify(csrf).csrfTokenRequestHandler(any());
        verify(sessions).sessionCreationPolicy(any());
        verify(exceptions).authenticationEntryPoint(any());
        verify(authorization).requestMatchers("/error", "/actuator/health");
        verify(oauth).jwt(any());
        verify(jwt).jwtAuthenticationConverter(any());

        final ArgumentCaptor<AuthenticationEntryPoint> entryPoint =
                ArgumentCaptor.forClass(AuthenticationEntryPoint.class);
        verify(exceptions).authenticationEntryPoint(entryPoint.capture());
        final HttpServletResponse response = mock(HttpServletResponse.class);
        entryPoint.getValue().commence(mock(HttpServletRequest.class), response, null);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void wrapsSecurityFilterChainBuildFailures() {
        final HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        when(http.build()).thenThrow(new IllegalArgumentException("broken"));
        final SecurityConfig config = new SecurityConfig();

        assertThatThrownBy(() -> config.filterChain(http))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to build security filter chain")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
