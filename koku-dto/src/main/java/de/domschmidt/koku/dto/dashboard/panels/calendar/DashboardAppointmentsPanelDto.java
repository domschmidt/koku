package de.domschmidt.koku.dto.dashboard.panels.calendar;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("appointments")
@Getter
public class DashboardAppointmentsPanelDto extends AbstractDashboardPanel {

    String headline;
    String emptyMessage;
    LocalDateTime start;
    LocalDateTime end;
    List<DashboardAppointmentsPanelListSourceDto> listSources;
}
