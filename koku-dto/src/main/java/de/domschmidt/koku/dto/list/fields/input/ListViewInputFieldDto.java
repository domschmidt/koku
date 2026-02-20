package de.domschmidt.koku.dto.list.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.KokuRoundedEnum;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("input")
@Getter
public class ListViewInputFieldDto extends AbstractListViewFieldDto<String> {

    @Builder.Default
    ListViewInputFieldTypeEnumDto type = ListViewInputFieldTypeEnumDto.TEXT;

    String label;
    KokuRoundedEnum rounded;
    KokuColorEnum backgroundColor;

    @Builder.Default
    String defaultValue = "";
}
