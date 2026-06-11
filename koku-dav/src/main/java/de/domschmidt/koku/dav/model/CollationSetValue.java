package de.domschmidt.koku.dav.model;

import java.util.List;

public record CollationSetValue(List<String> collations) implements DavPropertyValue {}
