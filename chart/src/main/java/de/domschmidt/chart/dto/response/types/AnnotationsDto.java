package de.domschmidt.chart.dto.response.types;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnotationsDto {

    List<AnnotationsAxesDto> xasis;
}
