package de.domschmidt.koku.carddav.model;

import java.util.List;

public record PrivilegeSetValue(List<DavPropertyName> privileges) implements DavPropertyValue {}
