package de.domschmidt.datatable.dto.type_specifics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class NumberSettingsDto {

    BigDecimal min;
    BigDecimal max;
    Integer integralDigits;
    Integer fractionalDigits;

}
