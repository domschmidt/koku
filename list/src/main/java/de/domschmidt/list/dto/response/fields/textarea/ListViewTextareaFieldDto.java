package de.domschmidt.list.dto.response.fields.textarea;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("textarea")
@Getter
public class ListViewTextareaFieldDto extends AbstractListViewFieldDto<String> {

    String label;
    @Builder.Default
    String defaultValue = "";

}
