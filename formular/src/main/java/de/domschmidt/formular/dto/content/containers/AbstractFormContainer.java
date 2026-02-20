package de.domschmidt.formular.dto.content.containers;

import de.domschmidt.formular.dto.content.IFormularContent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Data
public abstract class AbstractFormContainer implements IFormularContent {

    String id;

    public abstract void addContent(IFormularContent content);
}
