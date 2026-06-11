package de.domschmidt.koku.dav.model;

import java.util.List;

public record SupportedCalendarComponentSetValue(List<DavPropertyName> components) implements DavPropertyValue {}
