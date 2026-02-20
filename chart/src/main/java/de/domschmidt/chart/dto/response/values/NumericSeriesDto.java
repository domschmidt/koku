package de.domschmidt.chart.dto.response.values;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumericSeriesDto {

    String name;
    String group;
    List<BigDecimal> data;
}
