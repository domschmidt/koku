package de.domschmidt.koku.dto.list.fields.checkbox;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("checkbox")
@Getter
public class ListViewCheckboxFieldDto extends AbstractListViewFieldDto<Boolean> {

    String label;
    @Builder.Default
    Boolean defaultValue = Boolean.FALSE;

}
