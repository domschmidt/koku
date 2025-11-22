package de.domschmidt.listquery.factory.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;

public class StringFilter implements IListFilter {

    @Override
    public BooleanExpression buildGlobalSearchExpression(final Expression<?> expr, final String query) {
        if (query == null || query.isEmpty() || !(expr instanceof StringExpression castedExpr)) {
            return null;
        }

        return castedExpr.likeIgnoreCase("%" + query + "%");
    }

    @Override
    public BooleanExpression buildSearchExpression(final Expression<?> expr, final QueryPredicate query) {
        if (query == null || query.getSearchExpression() == null || query.getSearchExpression().isEmpty() || !(expr instanceof StringExpression castedExpr)) {
            return null;
        }

        final String searchExpression = query.getSearchExpression();
        BooleanExpression result = null;
        switch (query.getSearchOperator()) {
            case EQ -> result =  castedExpr.equalsIgnoreCase(searchExpression);
            case LESS -> result =  castedExpr.lower().lt(searchExpression.toLowerCase());
            case GREATER -> result =  castedExpr.lower().gt(searchExpression.toLowerCase());
            case LESS_OR_EQ -> result =  castedExpr.lower().loe(searchExpression.toLowerCase());
            case GREATER_OR_EQ -> result =  castedExpr.lower().goe(searchExpression.toLowerCase());
            case LIKE -> result =  castedExpr.likeIgnoreCase(searchExpression);
            case STARTS_WITH -> result =  castedExpr.startsWithIgnoreCase(searchExpression);
            case ENDS_WITH -> result =  castedExpr.endsWithIgnoreCase(searchExpression);
        }
        if (result != null && Boolean.TRUE.equals(query.getNegate())) {
            result = result.not();
        }
        return result;
    }
}
