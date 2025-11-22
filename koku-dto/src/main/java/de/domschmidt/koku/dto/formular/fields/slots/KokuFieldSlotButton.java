package de.domschmidt.koku.dto.formular.fields.slots;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.formular.dto.content.fields.slots.IFormFieldSlot;
import de.domschmidt.koku.dto.formular.buttons.ButtonDockableSettings;
import de.domschmidt.koku.dto.formular.buttons.EnumButtonStyle;
import de.domschmidt.koku.dto.formular.buttons.EnumLinkTarget;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@JsonTypeName("button")
public class KokuFieldSlotButton implements IFormFieldSlot {

    Boolean disabled;
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
