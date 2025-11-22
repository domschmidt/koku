package de.domschmidt.koku.dto.dashboard.panels.text;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("async-text")
@Getter
public class DashboardAsyncTextPanelDto extends AbstractDashboardPanel {

    String sourceUrl;

}