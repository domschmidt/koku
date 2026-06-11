package de.domschmidt.koku.carddav.model;

import java.util.List;

public record CollationSetValue(List<String> collations) implements DavPropertyValue {}
