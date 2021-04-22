package de.domschmidt.koku.dto.formular;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FormularDto {

    Long id;
    String description;
    List<FormularRowDto> rows;

}
