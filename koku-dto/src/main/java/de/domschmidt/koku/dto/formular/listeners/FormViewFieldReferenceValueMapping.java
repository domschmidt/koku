package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("field-reference")
@Getter
@FieldNameConstants
public class FormViewFieldReferenceValueMapping extends AbstractFormViewFieldValueMapping {

    AbstractFormViewEventPayloadFieldUpdateValueSource source;
}
