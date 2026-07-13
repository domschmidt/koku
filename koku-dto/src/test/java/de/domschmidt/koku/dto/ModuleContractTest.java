package de.domschmidt.koku.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.domschmidt.dashboard.dto.content.IDashboardContent;
import de.domschmidt.koku.dto.dashboard.containers.grid.DashboardGridContainerDto;
import de.domschmidt.koku.dto.date.YearWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ModuleContractTest {
    @Test
    void enumContractsExposeConstants() {
        assertTrue(de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.formular.buttons.EnumLinkTarget.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.formular.buttons.EnumButtonSize.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.formular.fields.input.EnumInputFormularFieldType.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.chart.filter.types.EnumInputChartFilterType.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.list.filters.ListViewToggleFilterDefaultStateEnum.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.KokuRoundedEnum.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.list.fields.input.ListViewInputFieldTypeEnumDto.values().length > 0);
        assertTrue(de.domschmidt.koku.dto.formular.events.FormNotificationEventSerenityEnumDto.values().length > 0);
    }

    @Test
    void yearWeekParsesFormatsComparesAndValidatesIsoWeeks() {
        final YearWeek parsed = YearWeek.parse("2026-W03");

        assertEquals(2026, parsed.getYear());
        assertEquals(3, parsed.getWeek());
        assertEquals("2026-W03", parsed.toString());
        assertEquals(parsed, YearWeek.from(LocalDate.of(2026, java.time.Month.JANUARY, 14)));
        assertTrue(parsed.compareTo(new YearWeek(2027, 1)) < 0);
        assertTrue(parsed.compareTo(new YearWeek(2026, 4)) < 0);
        assertEquals(0, parsed.compareTo(new YearWeek(2026, 3)));
        assertThrows(IllegalArgumentException.class, () -> YearWeek.parse("2026-03"));
        assertThrows(IllegalArgumentException.class, () -> new YearWeek(2026, 0));
        assertThrows(IllegalArgumentException.class, () -> new YearWeek(2026, 54));
    }

    @Test
    void dashboardGridCollectsContent() {
        final DashboardGridContainerDto grid =
                DashboardGridContainerDto.builder().cols(2).build();
        final IDashboardContent content = new IDashboardContent() {
            @Override
            public String getId() {
                return "content";
            }
        };

        grid.addContent(content);

        assertEquals(2, grid.getCols());
        assertEquals(java.util.List.of(content), grid.getContent());
    }
}
