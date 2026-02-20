package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("string-transformation")
public class StringTransformationConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {

    String transformPattern;
    Map<String, AbstractStringTransformationPatternParam> transformPatternParameters;
}
