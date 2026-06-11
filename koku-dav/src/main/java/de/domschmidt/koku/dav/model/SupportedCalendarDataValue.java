package de.domschmidt.koku.dav.model;

import java.util.List;

public record SupportedCalendarDataValue(List<CalendarDataType> calendarDataTypes) implements DavPropertyValue {}
