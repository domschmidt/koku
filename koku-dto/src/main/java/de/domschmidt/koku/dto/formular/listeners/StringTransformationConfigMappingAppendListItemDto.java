package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonTypeName("string-transformation")
public class StringTransformationConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {

    String transformPattern;
    Map<String, AbstractStringTransformationPatternParam> transformPatternParameters;
}
