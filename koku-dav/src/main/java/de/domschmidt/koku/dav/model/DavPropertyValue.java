package de.domschmidt.koku.dav.model;

public sealed interface DavPropertyValue
        permits CalendarDataValue,
                CollationSetValue,
                EmptyValue,
                HrefValue,
                PrivilegeSetValue,
                ResourceTypeValue,
                SupportedAddressDataValue,
                SupportedCalendarComponentSetValue,
                SupportedCalendarDataValue,
                SupportedReportSetValue,
                TextValue,
                VCardValue {}
