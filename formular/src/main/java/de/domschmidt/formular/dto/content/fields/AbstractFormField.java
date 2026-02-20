package de.domschmidt.formular.dto.content.fields;

import de.domschmidt.formular.dto.content.IFormularContent;
import de.domschmidt.formular.dto.content.fields.slots.IFormFieldSlot;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class AbstractFormField<T> implements IFormFieldDefault<T>, IFormularContent {

    String id;
    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    IFormFieldSlot prependOuter;
    IFormFieldSlot prependInner;
    IFormFieldSlot appendInner;
    IFormFieldSlot appendOuter;
}
