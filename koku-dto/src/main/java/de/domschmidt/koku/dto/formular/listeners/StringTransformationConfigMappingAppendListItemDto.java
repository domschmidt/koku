package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@JsonTypeName("string-transformation")
public class StringTransformationConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {

    String transformPattern;
    Map<String, AbstractStringTransformationPatternParam> transformPatternParameters;

}
