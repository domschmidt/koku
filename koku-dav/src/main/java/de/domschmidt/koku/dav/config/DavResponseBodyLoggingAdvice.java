package de.domschmidt.koku.dav.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice(basePackages = "de.domschmidt.koku.dav.controller")
@ConditionalOnProperty(prefix = "dav.logging", name = "bodies", havingValue = "true")
public class DavResponseBodyLoggingAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DavResponseBodyLoggingAdvice.class);
    private static final int MAX_LOGGED_BODY_LENGTH = 65_536;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            final HttpServletRequest httpRequest = servletRequest.getServletRequest();
            LOG.info(
                    "DAV response method={} uri={} body={}",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    formatBody(body));
        }
        return body;
    }

    private String formatBody(Object body) {
        if (body == null) {
            return "";
        }
        final String bodyString = String.valueOf(body);
        if (bodyString.length() <= MAX_LOGGED_BODY_LENGTH) {
            return bodyString;
        }
        return bodyString.substring(0, MAX_LOGGED_BODY_LENGTH) + "...[truncated]";
    }
}
