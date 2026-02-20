package de.domschmidt.listquery.dto.request;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
