package de.domschmidt.koku.dto.formular.fields.select;

import de.domschmidt.koku.dto.KokuColorEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Builder
@Getter
@FieldNameConstants
public class SelectFormularFieldPossibleValue {

    String id;
    String text;
    Boolean disabled;
    KokuColorEnum color;
    String category;
}
