package de.domschmidt.koku.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhoneNumberNormalizerTest {

    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer();

    @Test
    void normalizesValidGermanNumbersToE164() {
        assertThat(normalizer.normalize("030 123456")).isEqualTo("+4930123456");
    }

    @Test
    void preservesEmptyAndTrimsInvalidNumbers() {
        assertThat(normalizer.normalize(null)).isNull();
        assertThat(normalizer.normalize("  ")).isEqualTo("  ");
        assertThat(normalizer.normalize("  invalid  ")).isEqualTo("invalid");
        assertThat(normalizer.normalize("  123  ")).isEqualTo("123");
    }
}
