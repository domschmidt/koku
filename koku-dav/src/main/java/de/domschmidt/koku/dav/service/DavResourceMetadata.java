package de.domschmidt.koku.dav.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

final class DavResourceMetadata {

    static final String MAX_RESOURCE_SIZE_BYTES = "1048576";

    private DavResourceMetadata() {}

    static String etag(final Object... values) {
        return "\"" + Objects.hash(values) + "\"";
    }

    static String lastModified(final LocalDateTime updated) {
        final LocalDateTime timestamp = updated == null ? LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0) : updated;
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(timestamp.atOffset(ZoneOffset.UTC));
    }

    static int byteLength(final String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
