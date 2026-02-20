package de.domschmidt.list.dto.response.filters;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListViewFilterContentDto {

    String id;
    String valuePath;
    AbstractListViewFilterDto filterDefinition;
}
