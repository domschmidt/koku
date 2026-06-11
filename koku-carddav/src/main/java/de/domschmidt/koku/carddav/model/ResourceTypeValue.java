package de.domschmidt.koku.carddav.model;

import java.util.List;

public record ResourceTypeValue(List<DavPropertyName> resourceTypes) implements DavPropertyValue {}
