package de.domschmidt.koku.promotion.kafka.dto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void serdeContractIsConstructible() {
        assertNotNull(new PromotionKafkaDtoSerdes());
    }
}
