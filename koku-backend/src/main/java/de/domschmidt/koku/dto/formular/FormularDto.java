package de.domschmidt.koku.dto.formular;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(required = true)
    String description;
    @JsonProperty(required = true)
    DocumentContextDto context;
    Map<String, String> tags;
    @JsonProperty(required = true)
    List<FormularRowDto> rows;

}
