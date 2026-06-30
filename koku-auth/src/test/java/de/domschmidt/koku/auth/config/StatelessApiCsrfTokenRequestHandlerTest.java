package de.domschmidt.koku.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.DefaultCsrfToken;

class StatelessApiCsrfTokenRequestHandlerTest {

    private final StatelessApiCsrfTokenRequestHandler requestHandler = new StatelessApiCsrfTokenRequestHandler();

    @Test
    void authorizationHeaderResolvesToStatelessMarker() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        final String resolvedToken = requestHandler.resolveCsrfTokenValue(
                request, new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "cookie-token"));

        assertThat(resolvedToken).isEqualTo(StatelessApiCsrfTokenRepository.STATELESS_MARKER_VALUE);
    }

    @Test
    void requestsWithoutAuthorizationHeaderUseSubmittedCsrfToken() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-XSRF-TOKEN", "submitted-token");

        final String resolvedToken = requestHandler.resolveCsrfTokenValue(
                request, new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "cookie-token"));

        assertThat(resolvedToken).isEqualTo("submitted-token");
    }
}
