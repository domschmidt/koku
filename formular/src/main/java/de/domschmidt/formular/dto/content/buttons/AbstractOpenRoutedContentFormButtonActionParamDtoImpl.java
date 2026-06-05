package de.domschmidt.formular.dto.content.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
@EqualsAndHashCode(callSuper = true)
public class AbstractOpenRoutedContentFormButtonActionParamDtoImpl
        extends AbstractOpenRoutedContentFormButtonActionParamDto {

    String valuePath;
}
