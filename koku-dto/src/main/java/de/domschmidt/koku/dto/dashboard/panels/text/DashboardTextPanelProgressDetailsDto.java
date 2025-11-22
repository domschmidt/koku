package de.domschmidt.koku.dto.dashboard.panels.text;

import de.domschmidt.koku.dto.KokuColorEnum;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DashboardTextPanelProgressDetailsDto {

    String headline;
    KokuColorEnum headlineColor;
    String subHeadline;

}
