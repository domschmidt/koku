package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("field-reference")
@EqualsAndHashCode(callSuper = true)
@Getter
@FieldNameConstants
public class FormViewFieldReferenceValueMapping extends AbstractFormViewFieldValueMapping {

    AbstractFormViewEventPayloadFieldUpdateValueSource source;
}
