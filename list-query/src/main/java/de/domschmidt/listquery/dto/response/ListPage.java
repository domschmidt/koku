package de.domschmidt.listquery.dto.response;

import de.domschmidt.listquery.dto.request.ListFieldQuery;
import de.domschmidt.listquery.dto.response.items.ListItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ListPage {

    List<String> fieldSelection;
    Map<String, ListFieldQuery> fieldPredicates;
    String globalSearchTerm;

    List<ListItem> results = new ArrayList<>();

    Boolean hasMore;
    Integer pageIndex;
    Integer pageSize;
}
