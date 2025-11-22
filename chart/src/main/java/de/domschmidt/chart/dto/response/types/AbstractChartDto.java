package de.domschmidt.chart.dto.response.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.domschmidt.chart.dto.response.filters.AbstractChartFilterDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BarChartDto.class, name = "bar"),
        @JsonSubTypes.Type(value = LineChartDto.class, name = "line"),
        @JsonSubTypes.Type(value = PieChartDto.class, name = "pie")
})
public abstract class AbstractChartDto {
    String title;
    @Singular
    List<AbstractChartFilterDto> filters = new ArrayList<>();
    AnnotationsDto annotations;
}