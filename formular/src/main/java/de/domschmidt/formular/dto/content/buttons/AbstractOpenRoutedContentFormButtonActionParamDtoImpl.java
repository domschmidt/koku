package de.domschmidt.formular.dto.content.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class AbstractOpenRoutedContentFormButtonActionParamDtoImpl
        extends AbstractOpenRoutedContentFormButtonActionParamDto {

    String valuePath;
}
