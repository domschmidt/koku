package de.domschmidt.koku.carddav.model;

import java.util.List;

public record SupportedCalendarDataValue(List<CalendarDataType> calendarDataTypes) implements DavPropertyValue {}
