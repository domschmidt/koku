package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("static-value")
@Getter
@FieldNameConstants
public class FormViewEventPayloadStaticValueFieldUpdateValueSourceDto extends AbstractFormViewEventPayloadFieldUpdateValueSource {

    String value;

}
