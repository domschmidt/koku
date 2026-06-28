package de.domschmidt.koku.dav.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@ConditionalOnProperty(prefix = "dav.logging", name = "bodies", havingValue = "true")
public class DavBodyLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DavBodyLoggingFilter.class);
    private static final int MAX_LOGGED_BODY_LENGTH = 65_536;
    private static final Set<String> DAV_METHODS = Set.of("OPTIONS", "PROPFIND", "REPORT", "GET", "HEAD");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        final boolean isDavPath = "/".equals(requestUri)
                || requestUri.startsWith("/principals")
                || requestUri.startsWith("/addressbook")
                || requestUri.startsWith("/calendars");
        return !isDavPath || !DAV_METHODS.contains(request.getMethod().toUpperCase());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final OverflowAwareContentCachingRequestWrapper wrappedRequest =
                new OverflowAwareContentCachingRequestWrapper(request);
        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            final BodySummary requestBody = readBodySummary(wrappedRequest);
            LOG.info(
                    "DAV request completed status={} bodyLength={} bodyTruncated={}",
                    response.getStatus(),
                    requestBody.length(),
                    requestBody.truncated());
        }
    }

    private BodySummary readBodySummary(OverflowAwareContentCachingRequestWrapper request) {
        final byte[] content = request.getContentAsByteArray();
        return new BodySummary(content.length, request.isOverflow());
    }

    private record BodySummary(int length, boolean truncated) {}

    private static final class OverflowAwareContentCachingRequestWrapper extends ContentCachingRequestWrapper {

        private boolean overflow;

        private OverflowAwareContentCachingRequestWrapper(final HttpServletRequest request) {
            super(request, MAX_LOGGED_BODY_LENGTH);
        }

        @Override
        protected void handleContentOverflow(final int contentCacheLimit) {
            this.overflow = true;
        }

        private boolean isOverflow() {
            return this.overflow;
        }
    }
}
