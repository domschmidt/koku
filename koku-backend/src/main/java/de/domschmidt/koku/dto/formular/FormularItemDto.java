package de.domschmidt.koku.dto.formular;

import com.fasterxml.jackson.annotation.JsonProperty;
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
                @JsonSubTypes.Type(value = TextFormularItemDto.class, name = "TextFormularItemDto"),
                @JsonSubTypes.Type(value = CheckboxFormularItemDto.class, name = "CheckboxFormularItemDto"),
                @JsonSubTypes.Type(value = QrCodeFormularItemDto.class, name = "QrCodeFormularItemDto"),
                @JsonSubTypes.Type(value = DateFormularItemDto.class, name = "DateFormularItemDto"),
                @JsonSubTypes.Type(value = ActivityPriceListFormularItemDto.class, name = "ActivityPriceListFormularItemDto")
        }
)
public abstract class FormularItemDto {

    @JsonProperty(required = true)
    Long id;
    Long fieldDefinitionTypeId;
    Integer xs;
    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;
    FormularItemAlign align;

}
