package de.domschmidt.koku.dto.chart.filter.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.chart.dto.response.filters.AbstractChartFilterDto;
import java.time.YearMonth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeName("month-input")
public class MonthInputChartFilterDto extends AbstractChartFilterDto {

    String label;
    String placeholder;
    YearMonth value;
}
