package de.domschmidt.chart.dto.response.axes;

import de.domschmidt.chart.dto.response.axis.AbstractXAxisDto;
import de.domschmidt.chart.dto.response.axis.YAxisDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AxesDto {
    AbstractXAxisDto x;

    @Singular("y")
    List<YAxisDto> y;
}