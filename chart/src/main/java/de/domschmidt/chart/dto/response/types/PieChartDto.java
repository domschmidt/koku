package de.domschmidt.chart.dto.response.types;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PieChartDto extends AbstractChartDto {

    List<BigDecimal> series;
    List<String> labels;

}