package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("append-list")
@Getter
@FieldNameConstants
public class ConfigMappingAppendListDto extends AbstractConfigMappingDto {

    @Builder.Default
    List<AbstractConfigMappingAppendListItemDto> valueMapping = new ArrayList<>();

}
