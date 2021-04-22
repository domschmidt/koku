package de.domschmidt.koku.dto.panels;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartFilter;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartPanelDto extends PanelDto {

    ChartTypeEnum type;
    ChartData data;
    List<ChartFilter> filters;

}
