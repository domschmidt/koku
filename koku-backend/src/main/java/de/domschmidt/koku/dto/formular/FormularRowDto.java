package de.domschmidt.koku.dto.formular;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FormularRowDto {

    Long id;
    List<FormularItemDto> items;
    FormularRowAlignDto align;

}
