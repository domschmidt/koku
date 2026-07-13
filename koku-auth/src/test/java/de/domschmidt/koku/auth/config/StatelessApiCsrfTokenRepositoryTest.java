package de.domschmidt.koku.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

class StatelessApiCsrfTokenRepositoryTest {

    private final StatelessApiCsrfTokenRepository repository = new StatelessApiCsrfTokenRepository();

    @Test
    void bearerRequestsUseStatelessMarkerWithoutSavingCookie() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        final CsrfToken token = repository.generateToken(request);
        repository.saveToken(token, request, response);

        assertThat(token.getToken()).isEqualTo(StatelessApiCsrfTokenRepository.STATELESS_MARKER_VALUE);
        assertThat(repository.loadToken(request).getToken())
                .isEqualTo(StatelessApiCsrfTokenRepository.STATELESS_MARKER_VALUE);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
    }

    @Test
    void basicRequestsUseStatelessMarkerCaseInsensitively() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "bAsIc credentials");

        assertThat(repository.generateToken(request).getToken())
                .isEqualTo(StatelessApiCsrfTokenRepository.STATELESS_MARKER_VALUE);
    }

    @Test
    void cookieCsrfTokensAreHttpOnly() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        repository.saveToken(repository.generateToken(request), request, response);

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .singleElement()
                .asString()
                .contains("HttpOnly");
    }

    @Test
    void regularRequestsDelegateLoadAndStatelessRequestsCanClearCookies() {
        final MockHttpServletRequest regularRequest = new MockHttpServletRequest();
        assertThat(repository.loadToken(regularRequest)).isNull();

        final MockHttpServletRequest statelessRequest = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        statelessRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        repository.saveToken(
                new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "different-token"), statelessRequest, response);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).isNotEmpty();
    }

    @Test
    void authorizationSchemeWithoutCredentialsIsRecognized() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer");

        assertThat(StatelessApiCsrfTokenRepository.hasStatelessAuthorizationHeader(request))
                .isTrue();
    }
}
