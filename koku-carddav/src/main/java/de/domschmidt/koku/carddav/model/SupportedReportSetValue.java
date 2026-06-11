package de.domschmidt.koku.carddav.model;

import java.util.List;

public record SupportedReportSetValue(List<DavPropertyName> reports) implements DavPropertyValue {}
