package de.domschmidt.koku.dav.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DavValueObjectTest {

    private static final Instant TEN = Instant.parse("2026-07-14T10:00:00Z");
    private static final Instant ELEVEN = Instant.parse("2026-07-14T11:00:00Z");
    private static final Instant TWELVE = Instant.parse("2026-07-14T12:00:00Z");

    @Test
    void timeRangeSupportsOpenBoundsAndDetectsDisjointRanges() {
        assertThat(new DavTimeRange(null, null).overlaps(TEN, ELEVEN)).isTrue();
        assertThat(new DavTimeRange(TEN, null).overlaps(ELEVEN, TWELVE)).isTrue();
        assertThat(new DavTimeRange(null, ELEVEN).overlaps(TEN, TWELVE)).isTrue();
        assertThat(new DavTimeRange(TEN, ELEVEN).overlaps(ELEVEN, TWELVE)).isFalse();
        assertThat(new DavTimeRange(ELEVEN, TWELVE).overlaps(TEN, ELEVEN)).isFalse();
    }

    @Test
    void davMethodParsingIsCaseInsensitiveAndNullSafe() {
        assertThat(DavMethod.from("report")).isEqualTo(DavMethod.REPORT);
        assertThat(DavMethod.from("PATCH")).isEqualTo(DavMethod.UNKNOWN);
        assertThat(DavMethod.from(null)).isEqualTo(DavMethod.UNKNOWN);
    }
}
