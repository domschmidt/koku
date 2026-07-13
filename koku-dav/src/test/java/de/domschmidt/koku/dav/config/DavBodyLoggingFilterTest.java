package de.domschmidt.koku.dav.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class DavBodyLoggingFilterTest {

    private final DavBodyLoggingFilter filter = new DavBodyLoggingFilter();

    @Test
    void filtersSupportedDavMethodsOnEveryDavPath() {
        assertThat(shouldNotFilter("PROPFIND", "/")).isFalse();
        assertThat(shouldNotFilter("report", "/principals/user")).isFalse();
        assertThat(shouldNotFilter("GET", "/addressbook/user/contact.vcf")).isFalse();
        assertThat(shouldNotFilter("HEAD", "/calendars/user/default")).isFalse();
    }

    @Test
    void ignoresNonDavPathsAndUnsupportedMethods() {
        assertThat(shouldNotFilter("GET", "/actuator/health")).isTrue();
        assertThat(shouldNotFilter("POST", "/calendars/user/default")).isTrue();
    }

    @Test
    void cachesAndSummarizesNormalRequestBody() throws Exception {
        final MockHttpServletRequest request = request("REPORT", "/calendars/user/default");
        request.setContent("<calendar-query/>".getBytes(StandardCharsets.UTF_8));
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (wrappedRequest, wrappedResponse) -> {
            assertThat(wrappedRequest.getInputStream().readAllBytes())
                    .asString(StandardCharsets.UTF_8)
                    .isEqualTo("<calendar-query/>");
            response.setStatus(207);
        });

        assertThat(response.getStatus()).isEqualTo(207);
    }

    @Test
    void recordsContentOverflowWithoutChangingRequestBody() throws Exception {
        final byte[] body = "x".repeat(65_537).getBytes(StandardCharsets.UTF_8);
        final MockHttpServletRequest request = request("REPORT", "/addressbook/user");
        request.setContent(body);

        filter.doFilterInternal(
                request,
                new MockHttpServletResponse(),
                (wrappedRequest, wrappedResponse) -> assertThat(
                                wrappedRequest.getInputStream().readAllBytes())
                        .isEqualTo(body));
    }

    private boolean shouldNotFilter(final String method, final String uri) {
        return filter.shouldNotFilter(request(method, uri));
    }

    private static MockHttpServletRequest request(final String method, final String uri) {
        return new MockHttpServletRequest(method, uri);
    }
}
