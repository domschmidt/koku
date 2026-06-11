package de.domschmidt.koku.dav;

public final class DAVConstants {

    public static final String DAV_NAMESPACE = "DAV:";
    public static final String CARDDAV_NAMESPACE = "urn:ietf:params:xml:ns:carddav";
    public static final String CALDAV_NAMESPACE = "urn:ietf:params:xml:ns:caldav";
    public static final String CALENDAR_SERVER_NAMESPACE = "http://calendarserver.org/ns/";

    public static final String DAV_PROP_DISPLAYNAME = "displayname";
    public static final String DAV_PROP_RESOURCETYPE = "resourcetype";
    public static final String DAV_PROP_CURRENT_USER_PRINCIPAL = "current-user-principal";
    public static final String DAV_DEPTH_HEADER_NAME = "depth";
    public static final String DAV_PROP_GETETAG = "getetag";
    public static final String DAV_PROP_GETCONTENTLENGTH = "getcontentlength";
    public static final String DAV_PROP_GETCONTENTTYPE = "getcontenttype";
    public static final String DAV_PROP_GETLASTMODIFIED = "getlastmodified";
    public static final String DAV_PROP_OWNER = "owner";
    public static final String DAV_PROP_CURRENT_USER_PRIVILEGE_SET = "current-user-privilege-set";
    public static final String DAV_PROP_SUPPORTED_REPORT_SET = "supported-report-set";
    public static final String DAV_PROP_SYNC_TOKEN = "sync-token";
    public static final String DAV_RESOURCE_COLLECTION = "collection";
    public static final String DAV_RESOURCE_PRINCIPAL = "principal";
    public static final String DAV_PRIVILEGE_READ = "read";
    public static final String DAV_REPORT_SYNC_COLLECTION = "sync-collection";
    public static final String CARD_PROP_ADDRESS_DATA = "address-data";
    public static final String CARD_RESOURCE_ADDRESSBOOK = "addressbook";
    public static final String CARD_PROP_ADDRESSBOOK_DESCRIPTION = "addressbook-description";
    public static final String CARD_PROP_ADDRESSBOOK_HOME_SET = "addressbook-home-set";
    public static final String CARD_PROP_MAX_RESOURCE_SIZE = "max-resource-size";
    public static final String CARD_PROP_SUPPORTED_ADDRESS_DATA = "supported-address-data";
    public static final String CARD_PROP_SUPPORTED_COLLATION_SET = "supported-collation-set";
    public static final String CARD_REPORT_ADDRESSBOOK_MULTIGET = "addressbook-multiget";
    public static final String CARD_REPORT_ADDRESSBOOK_QUERY = "addressbook-query";
    public static final String CALDAV_RESOURCE_CALENDAR = "calendar";
    public static final String CALDAV_PROP_CALENDAR_DATA = "calendar-data";
    public static final String CALDAV_PROP_CALENDAR_HOME_SET = "calendar-home-set";
    public static final String CALDAV_PROP_MAX_RESOURCE_SIZE = "max-resource-size";
    public static final String CALDAV_PROP_SUPPORTED_CALENDAR_COMPONENT_SET = "supported-calendar-component-set";
    public static final String CALDAV_PROP_SUPPORTED_CALENDAR_DATA = "supported-calendar-data";
    public static final String CALDAV_PROP_SUPPORTED_COLLATION_SET = "supported-collation-set";
    public static final String CALDAV_PROP_CALENDAR_DESCRIPTION = "calendar-description";
    public static final String CALDAV_REPORT_CALENDAR_MULTIGET = "calendar-multiget";
    public static final String CALDAV_REPORT_CALENDAR_QUERY = "calendar-query";
    public static final String CALDAV_COMPONENT_VEVENT = "VEVENT";
    public static final String CALENDAR_SERVER_PROP_GETCTAG = "getctag";

    private DAVConstants() {}
}
