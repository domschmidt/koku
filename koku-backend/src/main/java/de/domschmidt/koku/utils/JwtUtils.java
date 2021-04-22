package de.domschmidt.koku.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public final class JwtUtils {

    public static final String JWT_COOKIE_NAME = "jwt";
    public static final String JWTR_COOKIE_NAME = "jwtr";

    private JwtUtils() {
    }

    public static String extractJwtValueFromRequest(final HttpServletRequest request, final String tokenName) {
        String result = null;
        final Cookie jwtCookie = CookieUtils.findCookie(request.getCookies(), tokenName);
        if (jwtCookie != null) {
            result = jwtCookie.getValue();
        }
        return result;
    }

}
