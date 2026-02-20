package de.domschmidt.listquery.factory.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;

public class UUIDFilter implements IListFilter {

    @Override
    public BooleanExpression buildGlobalSearchExpression(final Expression<?> expr, final String query) {
        if (query == null || query.isEmpty() || !(expr instanceof ComparableExpression)) {
            return null;
        }

        try {
            final StringTemplate castedExpr = Expressions.stringTemplate("CAST({0} AS text)", expr);
            return castedExpr.likeIgnoreCase("%" + query + "%");
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public BooleanExpression buildSearchExpression(final Expression<?> expr, final QueryPredicate query) {
        if (query == null
                || query.getSearchExpression() == null
                || query.getSearchExpression().isEmpty()
                || !(expr instanceof ComparableExpression)) {
            return null;
        }

        final String rawSearchExpr = query.getSearchExpression();
        final StringTemplate castedExpr = Expressions.stringTemplate("CAST({0} AS text)", expr);
        BooleanExpression result = null;
        switch (query.getSearchOperator()) {
            case LIKE, EQ -> result = castedExpr.eq(rawSearchExpr);
            case LESS -> result = castedExpr.lt(rawSearchExpr);
            case GREATER -> result = castedExpr.gt(rawSearchExpr);
            case LESS_OR_EQ -> result = castedExpr.loe(rawSearchExpr);
            case GREATER_OR_EQ -> result = castedExpr.goe(rawSearchExpr);
            case STARTS_WITH -> result = castedExpr.startsWithIgnoreCase(rawSearchExpr);
            case ENDS_WITH -> result = castedExpr.endsWithIgnoreCase(rawSearchExpr);
        }
        if (result != null && Boolean.TRUE.equals(query.getNegate())) {
            result = result.not();
        }
        return result;
    }
}
