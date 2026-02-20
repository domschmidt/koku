package de.domschmidt.chart.dto.response.axis;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YAxisDto {

    Boolean opposite;
    String text;
    List<String> seriesName = new ArrayList<>();
}
