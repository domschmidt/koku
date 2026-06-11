package de.domschmidt.koku.carddav.model;

import java.time.Instant;

public record DavTimeRange(Instant start, Instant end) {

    public boolean overlaps(final Instant candidateStart, final Instant candidateEnd) {
        final boolean startsBeforeRangeEnd = end == null || candidateStart.isBefore(end);
        final boolean endsAfterRangeStart = start == null || candidateEnd.isAfter(start);
        return startsBeforeRangeEnd && endsAfterRangeStart;
    }
}
