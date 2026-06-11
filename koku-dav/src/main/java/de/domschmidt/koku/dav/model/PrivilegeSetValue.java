package de.domschmidt.koku.dav.model;

import java.util.List;

public record PrivilegeSetValue(List<DavPropertyName> privileges) implements DavPropertyValue {}
