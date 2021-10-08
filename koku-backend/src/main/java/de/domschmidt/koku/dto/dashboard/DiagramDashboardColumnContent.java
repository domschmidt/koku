package de.domschmidt.koku.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartOptions;
import de.domschmidt.koku.dto.charts.ChartTextOverlay;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagramDashboardColumnContent implements IDashboardColumnContent {

    ChartTypeEnum type;
    ChartData data;
    ChartOptions options;
    ChartTextOverlay overlay;
    String label;

}
