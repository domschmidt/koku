package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("append-list")
@EqualsAndHashCode(callSuper = true)
@Getter
@FieldNameConstants
public class ConfigMappingAppendListDto extends AbstractConfigMappingDto {

    @Builder.Default
    List<AbstractConfigMappingAppendListItemDto> valueMapping = new ArrayList<>();
}
