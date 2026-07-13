package de.domschmidt.calendar;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void enumContractsExposeConstants() {
        assertTrue(
                de.domschmidt.calendar.dto.response.CalendarFormularContextSourceOffsetUnitEnumDto.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarFormularActionSubmitMethodEnumDto.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarListSourceColorEnumDto.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarHolidaySourceConfigCountryEnum.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarActionColorEnumDto.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarCallHttpItemActionMethodEnum.values().length > 0);
        assertTrue(
                de.domschmidt.calendar.dto.response.CalendarListSourceConfigReocurringRuleEnumDto.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarFormularContextSourceValueEnumDto.values().length > 0);
        assertTrue(de.domschmidt.calendar.dto.response.CalendarDataItemColorEnumDto.values().length > 0);
    }
}
