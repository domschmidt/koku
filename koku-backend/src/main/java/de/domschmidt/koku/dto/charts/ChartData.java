package de.domschmidt.koku.dto.charts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartData {

    List<String> labels;
    List<ChartDataSet> datasets;

}
