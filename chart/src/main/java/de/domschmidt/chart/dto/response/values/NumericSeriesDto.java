package de.domschmidt.chart.dto.response.values;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumericSeriesDto {

    String name;
    String group;
    List<BigDecimal> data;

}