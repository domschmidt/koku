package de.domschmidt.chart.dto.response.types;

import de.domschmidt.chart.dto.response.axes.AxesDto;
import de.domschmidt.chart.dto.response.values.NumericSeriesDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LineChartDto extends AbstractChartDto {

    AxesDto axes;
    List<NumericSeriesDto> series;

}