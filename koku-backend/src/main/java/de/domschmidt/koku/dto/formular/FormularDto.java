package de.domschmidt.koku.dto.formular;

import lombok.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FormularDto {

    Long id;
    String description;
    Map<String, String> tags;
    List<FormularRowDto> rows;

}
