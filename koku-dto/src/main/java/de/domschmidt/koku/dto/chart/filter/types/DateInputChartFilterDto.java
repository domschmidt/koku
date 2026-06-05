package de.domschmidt.koku.dto.chart.filter.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.chart.dto.response.filters.AbstractChartFilterDto;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeName("date-input")
public class DateInputChartFilterDto extends AbstractChartFilterDto {

    String label;
    String placeholder;
    LocalDate value;
}
