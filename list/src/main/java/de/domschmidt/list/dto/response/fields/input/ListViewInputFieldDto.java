package de.domschmidt.list.dto.response.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
    @Builder.Default
    String defaultValue = "";

}
