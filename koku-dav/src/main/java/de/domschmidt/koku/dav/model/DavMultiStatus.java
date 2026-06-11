package de.domschmidt.koku.dav.model;

import java.util.List;

public record DavMultiStatus(List<DavResponse> responses, String syncToken) {

    public DavMultiStatus(final List<DavResponse> responses) {
        this(responses, null);
    }
}
