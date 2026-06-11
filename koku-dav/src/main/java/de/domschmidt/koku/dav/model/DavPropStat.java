package de.domschmidt.koku.dav.model;

import java.util.List;

public record DavPropStat(int status, List<DavProperty> properties) {}
