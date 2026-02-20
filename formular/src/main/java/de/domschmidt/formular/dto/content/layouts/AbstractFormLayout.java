package de.domschmidt.formular.dto.content.layouts;

import de.domschmidt.formular.dto.content.IFormularContent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class AbstractFormLayout implements IFormularContent {

    String id;
}
