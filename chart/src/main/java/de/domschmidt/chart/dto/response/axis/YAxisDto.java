package de.domschmidt.chart.dto.response.axis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YAxisDto {

    Boolean opposite;
    String text;
    List<String> seriesName = new ArrayList<>();

}
