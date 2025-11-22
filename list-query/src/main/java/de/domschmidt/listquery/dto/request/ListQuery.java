package de.domschmidt.listquery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class ListQuery {

    String globalSearchTerm;
    List<String> fieldSelection;
    Map<String, ListFieldQuery> fieldPredicates;
    Integer page;
    Integer limit;

}
