package de.domschmidt.koku.dto.formular;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class ActivityPriceListFormularItemDto extends FormularItemDto {

    @JsonProperty(required = true)
    List<FormularRowDto> itemRows;
    @JsonProperty(required = true)
    List<FormularRowDto> groupRows;
    ActivityPriceListGroupByDto groupBy;
    List<FormularRowDto> evaluatedData;
    List<Long> sortByIds;

}
