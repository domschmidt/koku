package de.domschmidt.dashboard.dto.content.panels;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("async-chart")
@EqualsAndHashCode(callSuper = true)
@Getter
public class DashboardAsyncChartPanelDto extends AbstractDashboardPanel {

    String chartUrl;
}
