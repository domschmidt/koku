package de.domschmidt.koku.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;

public final class StatelessApiCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler delegate = new CsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            final HttpServletRequest request, final HttpServletResponse response, final Supplier<CsrfToken> csrfToken) {
        delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(final HttpServletRequest request, final CsrfToken csrfToken) {
        if (StatelessApiCsrfTokenRepository.hasStatelessAuthorizationHeader(request)) {
            return StatelessApiCsrfTokenRepository.STATELESS_MARKER_VALUE;
        }
        return delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}
