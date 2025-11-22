package de.domschmidt.dashboard.dto.content.panels;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("async-chart")
@Getter
public class DashboardAsyncChartPanelDto extends AbstractDashboardPanel {

    String chartUrl;

}
