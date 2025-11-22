package de.domschmidt.chart.dto.response.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnotationsDto {

    List<AnnotationsAxesDto> xasis;

}
