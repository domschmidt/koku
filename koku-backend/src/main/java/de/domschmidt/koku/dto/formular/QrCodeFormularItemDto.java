package de.domschmidt.koku.dto.formular;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class QrCodeFormularItemDto extends FormularItemDto {

    String value;
    Integer widthPercentage;
    Integer maxWidthInPx;

}
