package de.domschmidt.koku.dto.charts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartOptions {

    ChartScalesOptions scales;
    ChartElementsOptions elements;
    ChartPluginOptions plugins;

}
