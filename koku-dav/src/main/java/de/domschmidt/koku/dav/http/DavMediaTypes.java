package de.domschmidt.koku.dav.http;

public final class DavMediaTypes {

    public static final String ICALENDAR = "text/calendar";
    public static final String ICALENDAR_UTF8 = ICALENDAR + "; charset=UTF-8";
    public static final String VCARD = "text/vcard";
    public static final String VCARD_UTF8 = VCARD + "; charset=UTF-8";

    public static final String ICALENDAR_VERSION = "2.0";
    public static final String VCARD_VERSION = "3.0";

    private DavMediaTypes() {}
}
