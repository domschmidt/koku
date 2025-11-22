package de.domschmidt.koku.dto.dashboard.panels.text;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;
import de.domschmidt.koku.dto.KokuColorEnum;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("text")
@Getter
public class DashboardTextPanelDto extends AbstractDashboardPanel {

    KokuColorEnum color;
    String topHeadline;
    String headline;
    String subHeadline;

    List<DashboardTextPanelProgressDetailsDto> progressDetails;
    Short progress;

}
