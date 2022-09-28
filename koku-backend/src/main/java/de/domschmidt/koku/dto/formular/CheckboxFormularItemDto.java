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

public class CheckboxFormularItemDto extends FormularItemDto {

    boolean value;
    boolean readOnly;
    String label;
    String context;
    Integer fontSize;

}
