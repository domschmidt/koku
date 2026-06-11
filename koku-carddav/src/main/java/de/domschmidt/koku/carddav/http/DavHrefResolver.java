package de.domschmidt.koku.carddav.http;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DavHrefResolver {

    private static final String FORWARDED_PREFIX_HEADER = "X-Forwarded-Prefix";

    public String resolveHrefBasePath(final HttpServletRequest request) {
        final String forwardedPrefix = normalizePrefix(request.getHeader(FORWARDED_PREFIX_HEADER));
        if (StringUtils.isNotBlank(forwardedPrefix)) {
            return forwardedPrefix;
        }
        return normalizePrefix(request.getContextPath());
    }

    private String normalizePrefix(final String prefix) {
        if (StringUtils.isBlank(prefix) || "/".equals(prefix.trim())) {
            return "";
        }
        final String trimmed = prefix.trim();
        final String withLeadingSlash = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        return withLeadingSlash.endsWith("/")
                ? withLeadingSlash.substring(0, withLeadingSlash.length() - 1)
                : withLeadingSlash;
    }
}
