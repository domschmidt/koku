package de.domschmidt.listquery.factory;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import de.domschmidt.listquery.dto.request.QueryPredicate;

public interface IListFilter {

    BooleanExpression buildGlobalSearchExpression(Expression<?> fieldPath, String query);
    BooleanExpression buildSearchExpression(Expression<?> fieldPath, QueryPredicate query);

}
