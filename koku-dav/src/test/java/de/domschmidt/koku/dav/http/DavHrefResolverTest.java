package de.domschmidt.koku.dav.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class DavHrefResolverTest {

    @Test
    void normalizesForwardedAndContextPrefixes() {
        final DavHrefResolver resolver = new DavHrefResolver();
        final MockHttpServletRequest forwarded = new MockHttpServletRequest();
        forwarded.addHeader("X-Forwarded-Prefix", " gateway/ ");
        final MockHttpServletRequest context = new MockHttpServletRequest();
        context.setContextPath("/services");

        assertThat(resolver.resolveHrefBasePath(forwarded)).isEqualTo("/gateway");
        assertThat(resolver.resolveHrefBasePath(context)).isEqualTo("/services");
        assertThat(resolver.resolveHrefBasePath(new MockHttpServletRequest())).isEmpty();
    }
}
