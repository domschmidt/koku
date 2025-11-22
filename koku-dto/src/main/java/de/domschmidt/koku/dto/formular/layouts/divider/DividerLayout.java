package de.domschmidt.koku.dto.formular.layouts.divider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.layouts.AbstractFormLayout;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@JsonTypeName("divider")
@Getter
public class DividerLayout extends AbstractFormLayout {

    String text;

}
