package de.domschmidt.chart.dto.response.axes;

import de.domschmidt.chart.dto.response.axis.AbstractXAxisDto;
import de.domschmidt.chart.dto.response.axis.YAxisDto;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AxesDto {
    AbstractXAxisDto x;

    @Singular("y")
    List<YAxisDto> y;
}
