package de.domschmidt.koku.dto.chart.filter.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.chart.dto.response.filters.AbstractChartFilterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeName("input")
public class InputChartFilterDto extends AbstractChartFilterDto {

    @Builder.Default
    EnumInputChartFilterType type = EnumInputChartFilterType.TEXT;
    String label;
    String placeholder;
    YearMonth value;

}
