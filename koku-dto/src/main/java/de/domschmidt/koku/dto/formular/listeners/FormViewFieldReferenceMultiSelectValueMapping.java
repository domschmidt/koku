package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@JsonTypeName("append-list")
@Getter
@FieldNameConstants
public class FormViewFieldReferenceMultiSelectValueMapping extends AbstractFormViewFieldValueMapping {

    @Builder.Default
    final Map<String, AbstractFormViewEventPayloadFieldUpdateValueSource> targetPathMapping = new HashMap<>();

}