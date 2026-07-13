package de.domschmidt.chart;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void colorContractExposesConstants() {
        assertTrue(de.domschmidt.chart.dto.response.colors.ColorsEnumDto.values().length > 0);
    }
}
