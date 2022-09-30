package de.domschmidt.koku.dto.formular;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DocumentContextDto {

    @JsonProperty(required = true)
    DocumentContextEnumDto value;
    String description;

}
