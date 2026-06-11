package de.domschmidt.koku.carddav.model;

import de.domschmidt.koku.carddav.DAVConstants;

public final class DavPropertyNames {

    public static final DavPropertyName ADDRESS_DATA =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_ADDRESS_DATA);
    public static final DavPropertyName ADDRESSBOOK_DESCRIPTION =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_ADDRESSBOOK_DESCRIPTION);
    public static final DavPropertyName ADDRESSBOOK =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, "addressbook");
    public static final DavPropertyName ADDRESSBOOK_HOME_SET =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_ADDRESSBOOK_HOME_SET);
    public static final DavPropertyName COLLECTION = new DavPropertyName(DAVConstants.DAV_NAMESPACE, "collection");
    public static final DavPropertyName CURRENT_USER_PRINCIPAL =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_CURRENT_USER_PRINCIPAL);
    public static final DavPropertyName CURRENT_USER_PRIVILEGE_SET =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_CURRENT_USER_PRIVILEGE_SET);
    public static final DavPropertyName DISPLAYNAME =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_DISPLAYNAME);
    public static final DavPropertyName GETCONTENTTYPE =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_GETCONTENTTYPE);
    public static final DavPropertyName GETCONTENTLENGTH =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_GETCONTENTLENGTH);
    public static final DavPropertyName GETETAG =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_GETETAG);
    public static final DavPropertyName GETLASTMODIFIED =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_GETLASTMODIFIED);
    public static final DavPropertyName OWNER =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_OWNER);
    public static final DavPropertyName PRINCIPAL = new DavPropertyName(DAVConstants.DAV_NAMESPACE, "principal");
    public static final DavPropertyName READ_PRIVILEGE = new DavPropertyName(DAVConstants.DAV_NAMESPACE, "read");
    public static final DavPropertyName RESOURCETYPE =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_RESOURCETYPE);
    public static final DavPropertyName SUPPORTED_REPORT_SET =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_SUPPORTED_REPORT_SET);
    public static final DavPropertyName SYNC_COLLECTION =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, "sync-collection");
    public static final DavPropertyName SYNC_TOKEN =
            new DavPropertyName(DAVConstants.DAV_NAMESPACE, DAVConstants.DAV_PROP_SYNC_TOKEN);
    public static final DavPropertyName GETCTAG =
            new DavPropertyName(DAVConstants.CALENDAR_SERVER_NAMESPACE, DAVConstants.CALENDAR_SERVER_PROP_GETCTAG);
    public static final DavPropertyName MAX_RESOURCE_SIZE =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_MAX_RESOURCE_SIZE);
    public static final DavPropertyName PRINCIPAL_ADDRESS =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_PRINCIPAL_ADDRESS);
    public static final DavPropertyName SUPPORTED_ADDRESS_DATA =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_SUPPORTED_ADDRESS_DATA);
    public static final DavPropertyName SUPPORTED_COLLATION_SET =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, DAVConstants.CARD_PROP_SUPPORTED_COLLATION_SET);
    public static final DavPropertyName ADDRESSBOOK_MULTIGET =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, "addressbook-multiget");
    public static final DavPropertyName ADDRESSBOOK_QUERY =
            new DavPropertyName(DAVConstants.CARDDAV_NAMESPACE, "addressbook-query");
    public static final DavPropertyName CALENDAR = new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, "calendar");
    public static final DavPropertyName CALENDAR_DATA =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_CALENDAR_DATA);
    public static final DavPropertyName CALENDAR_DESCRIPTION =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_CALENDAR_DESCRIPTION);
    public static final DavPropertyName CALENDAR_HOME_SET =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_CALENDAR_HOME_SET);
    public static final DavPropertyName CALENDAR_MAX_RESOURCE_SIZE =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_MAX_RESOURCE_SIZE);
    public static final DavPropertyName SUPPORTED_CALENDAR_COMPONENT_SET = new DavPropertyName(
            DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_SUPPORTED_CALENDAR_COMPONENT_SET);
    public static final DavPropertyName SUPPORTED_CALENDAR_DATA =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_SUPPORTED_CALENDAR_DATA);
    public static final DavPropertyName CALENDAR_SUPPORTED_COLLATION_SET =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, DAVConstants.CALDAV_PROP_SUPPORTED_COLLATION_SET);
    public static final DavPropertyName CALENDAR_MULTIGET =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, "calendar-multiget");
    public static final DavPropertyName CALENDAR_QUERY =
            new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, "calendar-query");
    public static final DavPropertyName VEVENT = new DavPropertyName(DAVConstants.CALDAV_NAMESPACE, "VEVENT");

    private DavPropertyNames() {}
}
