package de.domschmidt.chart.dto.response.types;

import de.domschmidt.chart.dto.response.colors.ColorsEnumDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnotationsAxesDto {

    String x;
    ColorsEnumDto borderColor;
    AnnotationsAxesLabelDto label;
}
