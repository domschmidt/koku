package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("string-conversion")
public class StringConversionConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {

    String sourcePath;

}
