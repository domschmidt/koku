package de.domschmidt.koku.dto.formular.buttons;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ButtonDockableSettings {

    String title;
    String text;
    String icon;
    List<EnumButtonStyle> styles;
}
