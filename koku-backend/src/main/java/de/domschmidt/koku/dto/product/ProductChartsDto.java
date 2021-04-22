package de.domschmidt.koku.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.panels.ChartPanelDto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductChartsDto {

    ChartPanelDto mostSold;
    ChartPanelDto mostUsed;

}
