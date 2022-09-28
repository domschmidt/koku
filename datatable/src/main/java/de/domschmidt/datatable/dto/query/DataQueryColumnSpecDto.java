package de.domschmidt.datatable.dto.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataQueryColumnSpecDto {

    Object search;
    List<DataQueryAdvancedSearchDto> advancedSearchSpec = new ArrayList<>();
    List<Object> selectValues = new ArrayList<>();
    DataQueryColumnSortDirDto sortDir;
    Integer sortIdx;

}
