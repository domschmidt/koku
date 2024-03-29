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

public class TextFormularItemDto extends FormularItemDto {

    String text;
    boolean readOnly;
    Integer fontSize;

}
