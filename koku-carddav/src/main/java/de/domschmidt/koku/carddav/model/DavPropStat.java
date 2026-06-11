package de.domschmidt.koku.carddav.model;

import java.util.List;

public record DavPropStat(int status, List<DavProperty> properties) {}
