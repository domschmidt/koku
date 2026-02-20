package de.domschmidt.koku.dto.formular.fields.select;

import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.EnumLinkTarget;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SelectFormularButtonFieldSlot {

    EnumButtonType buttonType;
    String href;
    EnumLinkTarget hrefTarget;
    String title;
    String text;
    String icon;
    Boolean loading;
    Boolean disabled;
    List<EnumButtonStyle> styles;
}
