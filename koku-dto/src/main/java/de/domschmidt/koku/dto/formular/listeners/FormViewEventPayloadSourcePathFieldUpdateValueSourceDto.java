package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("source-path")
@EqualsAndHashCode(callSuper = true)
@Getter
@FieldNameConstants
public class FormViewEventPayloadSourcePathFieldUpdateValueSourceDto
        extends AbstractFormViewEventPayloadFieldUpdateValueSource {

    String sourcePath;
}
