package de.domschmidt.koku.business_logic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void enumContractsExposeConstants() {
        assertTrue(
                de.domschmidt.koku.business_logic.dto.KokuBusinessRuleFieldReferenceUpdateModeEnum.values().length > 0);
        assertTrue(
                de.domschmidt.koku.business_logic.dto.KokuBusinessRuleFormularActionSubmitMethodEnumDto.values().length
                        > 0);
        assertTrue(de.domschmidt.koku.business_logic.dto.KokuBusinessRuleFieldReferenceListenerEventEnum.values().length
                > 0);
        assertTrue(
                de.domschmidt.koku.business_logic.dto.KokuBusinessRuleCallHttpEndpointMethodEnum.values().length > 0);
    }
}
