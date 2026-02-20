package de.domschmidt.koku.dto.formular.layouts.icon;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.layouts.AbstractFormLayout;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("icon")
@Getter
public class IconLayout extends AbstractFormLayout {

    String icon;
}
