package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.AbstractFormViewGlobalEventListenerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("source-update-via-payload")
@Getter
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class FormViewEventPayloadSourceUpdateGlobalEventListenerDto extends AbstractFormViewGlobalEventListenerDto {
    String idPath;
}
