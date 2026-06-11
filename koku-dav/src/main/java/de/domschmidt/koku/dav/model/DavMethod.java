package de.domschmidt.koku.dav.model;

public enum DavMethod {
    GET,
    HEAD,
    OPTIONS,
    PROPFIND,
    REPORT,
    UNKNOWN;

    public static DavMethod from(final String method) {
        try {
            return DavMethod.valueOf(method.toUpperCase());
        } catch (final Exception e) {
            return UNKNOWN;
        }
    }
}
