package de.domschmidt.listquery.factory.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;

public class BooleanFilter implements IListFilter {

    @Override
    public BooleanExpression buildGlobalSearchExpression(final Expression<?> expr, final String query) {
        if (query == null || query.isEmpty() || !(expr instanceof BooleanExpression castedExpr)) {
            return null;
        }

        return castedExpr.stringValue().likeIgnoreCase("%" + query + "%");
    }

    @Override
    public BooleanExpression buildSearchExpression(final Expression<?> expr, final QueryPredicate query) {
        String searchExpressionRaw = query.getSearchExpression();
        if (query == null || searchExpressionRaw == null || searchExpressionRaw.isEmpty() || !(expr instanceof BooleanExpression castedExpr)) {
            return null;
        }

        final Boolean searchExpression = Boolean.valueOf(searchExpressionRaw);
        BooleanExpression result = null;
        switch (query.getSearchOperator()) {
            case EQ, LIKE -> result =  castedExpr.eq(searchExpression);
            case LESS -> result =  castedExpr.lt(searchExpression);
            case GREATER -> result =  castedExpr.gt(searchExpression);
            case LESS_OR_EQ -> result =  castedExpr.loe(searchExpression);
            case GREATER_OR_EQ -> result =  castedExpr.goe(searchExpression);
            case STARTS_WITH -> result =  castedExpr.stringValue().startsWithIgnoreCase(searchExpressionRaw);
            case ENDS_WITH -> result =  castedExpr.stringValue().endsWithIgnoreCase(searchExpressionRaw);
        }
        if (result != null && Boolean.TRUE.equals(query.getNegate())) {
            result = result.not();
        }
        return result;
    }
}
