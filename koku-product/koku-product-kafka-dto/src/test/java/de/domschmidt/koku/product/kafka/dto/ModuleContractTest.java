package de.domschmidt.koku.product.kafka.dto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void serdeContractsAreConstructible() {
        assertNotNull(new ProductKafkaDtoSerdes());
        assertNotNull(new ProductManufacturerKafkaDtoSerdes());
    }
}
