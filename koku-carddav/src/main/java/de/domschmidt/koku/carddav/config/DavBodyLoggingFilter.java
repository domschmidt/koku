package de.domschmidt.koku.carddav.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        final ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request, MAX_LOGGED_BODY_LENGTH);
        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            final String requestBody = readBody(wrappedRequest);
            LOG.info(
                    "DAV request method={} uri={} status={} body={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    requestBody);
        }
    }

    private String readBody(ContentCachingRequestWrapper request) {
        final byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        final Charset charset = request.getCharacterEncoding() == null
                ? StandardCharsets.UTF_8
                : Charset.forName(request.getCharacterEncoding());
        final int length = Math.min(content.length, MAX_LOGGED_BODY_LENGTH);
        final String body = new String(content, 0, length, charset);
        return content.length > MAX_LOGGED_BODY_LENGTH ? body + "...[truncated]" : body;
    }
}
