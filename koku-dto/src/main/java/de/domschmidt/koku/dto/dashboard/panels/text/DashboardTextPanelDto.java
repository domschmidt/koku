package de.domschmidt.koku.dto.dashboard.panels.text;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.dashboard.dto.content.panels.AbstractDashboardPanel;
import de.domschmidt.koku.dto.KokuColorEnum;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("text")
@Getter
public class DashboardTextPanelDto extends AbstractDashboardPanel {

    KokuColorEnum color;
    String topHeadline;
    String headline;
    String subHeadline;

    List<DashboardTextPanelProgressDetailsDto> progressDetails;
    List<DashboardTextPanelExplanationItemDto> explanations;
    Short progress;
}
