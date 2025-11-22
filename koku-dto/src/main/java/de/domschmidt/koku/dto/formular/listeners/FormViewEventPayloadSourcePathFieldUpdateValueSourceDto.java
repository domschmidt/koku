package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("source-path")
@Getter
@FieldNameConstants
public class FormViewEventPayloadSourcePathFieldUpdateValueSourceDto extends AbstractFormViewEventPayloadFieldUpdateValueSource {

    String sourcePath;

}
