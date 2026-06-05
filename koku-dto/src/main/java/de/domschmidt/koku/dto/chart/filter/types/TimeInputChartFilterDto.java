package de.domschmidt.koku.dto.chart.filter.types;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.chart.dto.response.filters.AbstractChartFilterDto;
import java.time.LocalTime;
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
@JsonTypeName("time-input")
public class TimeInputChartFilterDto extends AbstractChartFilterDto {

    String label;
    String placeholder;
    LocalTime value;
}
