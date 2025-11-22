package de.domschmidt.list.dto.response.fields;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListViewFieldContentDto {

    String id;
    String valuePath;
    AbstractListViewFieldDto<?> fieldDefinition;

}
