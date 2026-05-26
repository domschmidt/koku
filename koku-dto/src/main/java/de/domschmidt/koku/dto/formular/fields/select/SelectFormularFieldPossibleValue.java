package de.domschmidt.koku.dto.formular.fields.select;

import de.domschmidt.koku.contracts.dto.KokuColor;
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
    KokuColor color;
    String category;
}
