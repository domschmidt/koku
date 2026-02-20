package de.domschmidt.listquery.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ListFieldQuery {

    List<QueryPredicate> predicates;
    EnumQuerySort sort;
    Integer sortRanking;
}
