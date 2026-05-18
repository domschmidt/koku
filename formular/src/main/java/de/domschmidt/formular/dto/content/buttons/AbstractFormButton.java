package de.domschmidt.formular.dto.content.buttons;

import de.domschmidt.formular.dto.content.IFormularContent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class AbstractFormButton implements IFormularContent {

    String id;
    Boolean disabled;
    EnumButtonType buttonType;
}
