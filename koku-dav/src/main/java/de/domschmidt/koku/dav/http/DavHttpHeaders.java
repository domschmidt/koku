package de.domschmidt.koku.dav.http;

public final class DavHttpHeaders {

    public static final String DAV = "DAV";
    public static final String DAV_COMPLIANCE = "1, 2, addressbook, calendar-access";
    public static final String MS_AUTHOR_VIA = "MS-Author-Via";
    public static final String MS_AUTHOR_VIA_VALUE = "DAV";
    public static final String ALLOW = "Allow";
    public static final String ALLOW_VALUE = "OPTIONS, PROPFIND, REPORT, GET, HEAD";

    private DavHttpHeaders() {}
}
