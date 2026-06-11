package de.domschmidt.koku.carddav.model;

import java.util.List;

public record SupportedCalendarComponentSetValue(List<DavPropertyName> components) implements DavPropertyValue {}
