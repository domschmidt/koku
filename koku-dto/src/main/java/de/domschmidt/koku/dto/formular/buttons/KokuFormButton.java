package de.domschmidt.koku.dto.formular.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.buttons.AbstractFormButton;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("button")
@Data
public class KokuFormButton extends AbstractFormButton {

    EnumButtonType buttonType;
    String href;
    EnumLinkTarget hrefTarget;
    String title;
    String text;
    String icon;
    Boolean loading;
    Boolean dockable;
    ButtonDockableSettings dockableSettings;
    List<EnumButtonStyle> styles;
}
