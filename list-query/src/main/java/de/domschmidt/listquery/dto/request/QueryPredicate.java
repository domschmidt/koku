package de.domschmidt.listquery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueryPredicate {

    String searchExpression;
    EnumSearchOperator searchOperator;
    EnumSearchOperatorHint searchOperatorHint;
    Boolean negate;
    String orGroupIdentifier;

}
