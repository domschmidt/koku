package de.domschmidt.listquery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ListFieldQuery {

    List<QueryPredicate> predicates;
    EnumQuerySort sort;
    Integer sortRanking;

}
