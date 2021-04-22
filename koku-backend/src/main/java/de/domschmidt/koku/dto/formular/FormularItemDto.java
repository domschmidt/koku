package de.domschmidt.koku.dto.formular;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(value = SVGFormularItemDto.class, name = "SVGFormularItemDto"),
                @JsonSubTypes.Type(value = SignatureFormularItemDto.class, name = "SignatureFormularItemDto"),
                @JsonSubTypes.Type(value = TextFormularItemDto.class, name = "TextFormularItemDto")
        }
)
public abstract class FormularItemDto {

    Long id;
    Long fieldDefinitionTypeId;
    Integer xs;
    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;
    FormularItemAlign align;

}
