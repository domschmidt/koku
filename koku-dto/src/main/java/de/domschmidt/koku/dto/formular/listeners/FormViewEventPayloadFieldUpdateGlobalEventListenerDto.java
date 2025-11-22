package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.AbstractFormViewGlobalEventListenerDto;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@JsonTypeName("field-update-via-payload")
@Getter
@FieldNameConstants
public class FormViewEventPayloadFieldUpdateGlobalEventListenerDto extends AbstractFormViewGlobalEventListenerDto {

    Map<String, AbstractFormViewFieldValueMapping> fieldValueMapping;
    Map<String, FormViewFieldConfigMapping> configMapping;

}
