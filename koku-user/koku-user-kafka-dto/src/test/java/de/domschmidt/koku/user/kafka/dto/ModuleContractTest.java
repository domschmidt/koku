package de.domschmidt.koku.user.kafka.dto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void serdeContractsAreConstructible() {
        assertNotNull(new UserKafkaDtoSerdes());
        assertNotNull(new UserAppointmentKafkaDtoSerdes());
    }
}
