package de.domschmidt.datatable.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTableDto {

    List<DataTableColumnDto<?, ?>> columns;
    List<Map<String, Object>> rows;
    String tableName;
    Integer pageSize;
    Integer page;
    Long total;
    Integer totalPages;

}
