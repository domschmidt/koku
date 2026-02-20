package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-inline-content")
@SuperBuilder
@Data
public class ListViewOpenInlineContentActionDto extends AbstractListViewActionDto {

    AbstractListViewContentDto inlineContent;
}
