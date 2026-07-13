package de.domschmidt.koku.dav.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

class DavResponseBodyLoggingAdviceTest {

    private final DavResponseBodyLoggingAdvice advice = new DavResponseBodyLoggingAdvice();

    @Test
    void supportsEveryResponseConverter() {
        assertThat(advice.supports(mock(MethodParameter.class), converterType()))
                .isTrue();
    }

    @Test
    void preservesNullNormalAndOversizedResponseBodies() {
        final MockHttpServletRequest servletRequest = new MockHttpServletRequest("REPORT", "/calendars/me");
        final ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        final ServerHttpResponse response = mock(ServerHttpResponse.class);
        final String normal = "calendar";
        final String oversized = "x".repeat(65_537);

        assertThat(write(null, request, response)).isNull();
        assertThat(write(normal, request, response)).isSameAs(normal);
        assertThat(write(oversized, request, response)).isSameAs(oversized);
    }

    @Test
    void preservesBodyForNonServletRequests() {
        final Object body = new Object();

        assertThat(write(body, mock(ServerHttpRequest.class), mock(ServerHttpResponse.class)))
                .isSameAs(body);
    }

    private Object write(Object body, ServerHttpRequest request, ServerHttpResponse response) {
        return advice.beforeBodyWrite(
                body, mock(MethodParameter.class), MediaType.APPLICATION_XML, converterType(), request, response);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends HttpMessageConverter<?>> converterType() {
        return (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
    }
}
