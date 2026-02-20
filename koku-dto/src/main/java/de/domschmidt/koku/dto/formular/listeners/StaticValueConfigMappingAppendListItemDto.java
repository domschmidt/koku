package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("static-value")
public class StaticValueConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {

    Object value;
}
