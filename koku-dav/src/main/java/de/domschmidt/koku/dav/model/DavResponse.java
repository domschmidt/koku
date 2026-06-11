package de.domschmidt.koku.dav.model;

import java.util.List;

public record DavResponse(String href, List<DavPropStat> propStats, Integer status) {

    public DavResponse(final String href, final List<DavPropStat> propStats) {
        this(href, propStats, null);
    }

    public static DavResponse notFound(final String href) {
        return new DavResponse(href, List.of(), 404);
    }
}
