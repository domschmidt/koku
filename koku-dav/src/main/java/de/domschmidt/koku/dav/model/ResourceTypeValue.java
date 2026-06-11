package de.domschmidt.koku.dav.model;

import java.util.List;

public record ResourceTypeValue(List<DavPropertyName> resourceTypes) implements DavPropertyValue {}
