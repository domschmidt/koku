package de.domschmidt.koku.dto.charts;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.KokuColor;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentedData {

    KokuColor backgroundColor;
    KokuColor borderColor;
    Boolean borderDashed;

}
