package de.domschmidt.koku.dto.formular.buttons;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public class ButtonDockableSettings {

    String title;
    String text;
    String icon;
    List<EnumButtonStyle> styles;

}
