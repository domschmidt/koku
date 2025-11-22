package de.domschmidt.formular.dto.content.buttons;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@SuperBuilder
public abstract class AbstractOpenRoutedContentFormButtonActionParamDto {
    String param;
}
