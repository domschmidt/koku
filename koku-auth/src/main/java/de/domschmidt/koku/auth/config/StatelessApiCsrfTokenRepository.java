package de.domschmidt.koku.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

public final class StatelessApiCsrfTokenRepository implements CsrfTokenRepository {

    /*
     * Browser forms cannot send Authorization headers cross-site, and browser CORS requests
     * with Authorization are preflighted. Cookie-authenticated requests still use normal CSRF tokens.
     */
    static final String STATELESS_MARKER_VALUE = "authorization-header-present";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String CSRF_PARAMETER_NAME = "_csrf";
    private static final Set<String> STATELESS_AUTHENTICATION_SCHEMES = Set.of("bearer", "basic");

    private final CsrfTokenRepository delegate;

    public StatelessApiCsrfTokenRepository() {
        this(cookieCsrfTokenRepository());
    }

    StatelessApiCsrfTokenRepository(final CsrfTokenRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CsrfToken generateToken(final HttpServletRequest request) {
        if (hasStatelessAuthorizationHeader(request)) {
            return statelessMarker();
        }
        return delegate.generateToken(request);
    }

    @Override
    public void saveToken(final CsrfToken token, final HttpServletRequest request, final HttpServletResponse response) {
        if (hasStatelessAuthorizationHeader(request) && isStatelessMarker(token)) {
            return;
        }
        delegate.saveToken(token, request, response);
    }

    @Override
    public CsrfToken loadToken(final HttpServletRequest request) {
        if (hasStatelessAuthorizationHeader(request)) {
            return statelessMarker();
        }
        return delegate.loadToken(request);
    }

    static boolean hasStatelessAuthorizationHeader(final HttpServletRequest request) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            return false;
        }
        final int separatorIndex = authorization.indexOf(' ');
        final String scheme = separatorIndex < 0 ? authorization : authorization.substring(0, separatorIndex);
        return STATELESS_AUTHENTICATION_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT));
    }

    private static CsrfToken statelessMarker() {
        return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, STATELESS_MARKER_VALUE);
    }

    private static CsrfTokenRepository cookieCsrfTokenRepository() {
        final CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
        repository.setCookieCustomizer(cookie -> cookie.httpOnly(true));
        return repository;
    }

    private static boolean isStatelessMarker(final CsrfToken token) {
        return token != null && STATELESS_MARKER_VALUE.equals(token.getToken());
    }
}
