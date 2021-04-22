package de.domschmidt.koku.utils;

import javax.servlet.http.Cookie;

/**
 * Helper class for reading and writing cookies.
 */
public final class CookieUtils {

    private static final String GLOBAL_COOKIE_PATH = "/";

    private CookieUtils() {
    }

    public static Cookie findCookie(final Cookie[] cookies, final String cookieName) {
        Cookie cookie = null;
        if (cookies != null) {
            for (final Cookie currentCookie : cookies) {
                if (cookieName.equals(currentCookie.getName())) {
                    cookie = currentCookie;
                    break;
                }
            }
        }
        return cookie;
    }

    public static Cookie generateCookie(final String cookieName, final boolean secure, final int maxAge, final String value) {
        final Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setMaxAge(maxAge);
        cookie.setPath(GLOBAL_COOKIE_PATH);
        return cookie;
    }

    /**
     * invalidate a Cookie by setting an expired max age value (unix timestamp 0 -> 1970)
     *
     * @param cookie
     *            the token to be invalidated
     * @return the invalidated cookie
     */
    public static Cookie invalidateCookie(final Cookie cookie) {
        cookie.setMaxAge(0);
        cookie.setPath(GLOBAL_COOKIE_PATH);
        cookie.setValue(""); // saves bandwidth
        return cookie;
    }

}