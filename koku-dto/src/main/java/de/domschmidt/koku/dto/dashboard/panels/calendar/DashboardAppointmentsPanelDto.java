package de.domschmidt.koku.dto.dashboard.panels.calendar;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

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
