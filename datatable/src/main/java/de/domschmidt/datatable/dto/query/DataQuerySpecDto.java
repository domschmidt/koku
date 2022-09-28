package de.domschmidt.datatable.dto.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataQuerySpecDto {

    Integer page;
    Integer total;
    Map<String, DataQueryColumnSpecDto> columnSpecByColumnId;
    String globalSearch;

}
