package de.domschmidt.datatable.dto.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataQueryAdvancedSearchDto {

    Object search;
    DataQueryColumnOPDto customOp;

}
