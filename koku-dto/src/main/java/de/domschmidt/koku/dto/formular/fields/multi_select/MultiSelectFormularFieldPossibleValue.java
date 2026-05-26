package de.domschmidt.koku.dto.formular.fields.multi_select;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.domschmidt.koku.contracts.dto.KokuColor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Builder
@Getter
@FieldNameConstants
public class MultiSelectFormularFieldPossibleValue {

    String id;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    Object valueMapping;

    String text;
    Boolean disabled;
    KokuColor color;
    String category;
}
