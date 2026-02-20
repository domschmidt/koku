package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.AbstractFormViewGlobalEventListenerDto;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("field-update-via-payload")
@Getter
@FieldNameConstants
public class FormViewEventPayloadFieldUpdateGlobalEventListenerDto extends AbstractFormViewGlobalEventListenerDto {

    Map<String, AbstractFormViewFieldValueMapping> fieldValueMapping;
    Map<String, FormViewFieldConfigMapping> configMapping;
}
