package de.domschmidt.koku.dav.model;

import java.util.List;

public record SupportedReportSetValue(List<DavPropertyName> reports) implements DavPropertyValue {}
