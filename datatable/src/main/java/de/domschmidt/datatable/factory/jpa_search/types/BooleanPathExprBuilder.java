package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

public class BooleanPathExprBuilder implements IExpressionSearch<BooleanExpression> {
    @Override
    public BooleanExpression getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!BooleanExpression.class.isAssignableFrom(expression.getClass())) {
            throw new DataTableUnsupportedExpressionTypeException("BooleanExpression is not assignable to " + expression.getClass());
        }
        return (BooleanExpression) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        if (Boolean.TRUE.equals(rawSearchValue) || Boolean.FALSE.equals(rawSearchValue)) {
            return getCastedExpression(expression).eq(Boolean.TRUE.equals(rawSearchValue));
        }
        throw new DataTableUnsupportedExpressionTypeException("Cannot extract Boolean of given search value \"" + rawSearchValue + "\"");
    }

    @Override
    public OrderSpecifier<?> createOrderBySpecifier(
            final Expression<?> expression,
            final boolean asc
    ) throws DataTableUnsupportedExpressionTypeException {
        if (asc) {
            return getCastedExpression(expression).asc();
        } else {
            return getCastedExpression(expression).desc();
        }
    }

}
