package de.domschmidt.koku.dto.charts;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.KokuColor;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartDataSet {

    List<BigDecimal> data;
    List<SegmentedData> segmentedData;
    Boolean fill;
    List<String> backgroundColor;
    String label;
    List<KokuColor> colors;
    ChartDataLabelsConfig datalabels;

}
