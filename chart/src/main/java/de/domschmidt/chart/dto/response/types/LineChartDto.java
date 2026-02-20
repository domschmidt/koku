package de.domschmidt.chart.dto.response.types;

import de.domschmidt.chart.dto.response.axes.AxesDto;
import de.domschmidt.chart.dto.response.values.NumericSeriesDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LineChartDto extends AbstractChartDto {

    AxesDto axes;
    List<NumericSeriesDto> series;
}
