package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonTypeName("static-value")
public class StaticValueConfigMappingAppendListItemDto extends AbstractConfigMappingAppendListItemDto {

    Object value;
}
