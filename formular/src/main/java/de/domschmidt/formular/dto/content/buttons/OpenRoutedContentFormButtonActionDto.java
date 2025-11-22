package de.domschmidt.formular.dto.content.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("open-routed-content")
@Data
public class OpenRoutedContentFormButtonActionDto extends AbstractFormButtonButtonAction {

    String route;
    List<AbstractOpenRoutedContentFormButtonActionParamDto> params;

}
