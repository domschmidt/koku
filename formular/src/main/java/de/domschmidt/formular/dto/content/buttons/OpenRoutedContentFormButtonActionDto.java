package de.domschmidt.formular.dto.content.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("open-routed-content")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpenRoutedContentFormButtonActionDto extends AbstractFormButtonButtonAction {

    String route;
    List<AbstractOpenRoutedContentFormButtonActionParamDto> params;
}
