package de.domschmidt.koku.carddav.model;

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
