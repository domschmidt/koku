package de.domschmidt.formular.dto.content.buttons;

import de.domschmidt.formular.dto.content.IFormularContent;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public abstract class AbstractFormButton implements IFormularContent {

    String id;
    Boolean disabled;
    EnumButtonType buttonType;
    @Singular
    List<AbstractFormButtonButtonAction> postProcessingActions;

}
