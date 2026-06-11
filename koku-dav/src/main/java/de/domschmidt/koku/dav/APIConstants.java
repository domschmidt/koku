package de.domschmidt.koku.dav;

public final class APIConstants {

    public static final String PRINCIPALS_PATH = "/principals";
    public static final String ADDRESSBOOK_PATH = "/addressbook";
    public static final String CALENDAR_PATH = "/calendars";
    public static final String APPOINTMENTS_SEGMENT = "appointments";
    public static final String PRIVATE_SEGMENT = "private";
    public static final String PRINCIPALS_PATTERN = PRINCIPALS_PATH + "/**";
    public static final String ADDRESSBOOK_PATTERN = ADDRESSBOOK_PATH + "/**";
    public static final String CALENDAR_PATTERN = CALENDAR_PATH + "/**";
    public static final String ROOT_PATH = "/";

    private APIConstants() {}
}
