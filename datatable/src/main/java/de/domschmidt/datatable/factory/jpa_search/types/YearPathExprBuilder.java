package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.time.Year;

public class YearPathExprBuilder implements IExpressionSearch<ComparableExpression<Year>> {

    @Override
    public ComparableExpression<Year> getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!ComparablePath.class.isAssignableFrom(expression.getClass()) || !expression.getType().equals(Year.class)) {
            throw new DataTableUnsupportedExpressionTypeException("ComparablePath is not assignable to " + expression.getClass());
        }
        return (ComparableExpression<Year>) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        final Year castedValue;
        try {
            castedValue = Year.of(Integer.parseInt(String.valueOf(rawSearchValue)));
        } catch (final NumberFormatException e) {
            throw new DataTableUnsupportedExpressionTypeException("Cannot extract Year of given search value \"" + rawSearchValue + "\"", e);
        }
        final BooleanExpression result;

        final DataQueryColumnOPDto op;
        if (customOp == null) {
            // default
            op = DataQueryColumnOPDto.EQ;
        } else {
            op = customOp;
        }

        switch (op) {
            case LT:
                result = getCastedExpression(expression).lt(castedValue);
                break;
            case LOE:
                result = getCastedExpression(expression).loe(castedValue);
                break;
            case GT:
                result = getCastedExpression(expression).gt(castedValue);
                break;
            case GOE:
                result = getCastedExpression(expression).goe(castedValue);
                break;
            case EW:
            case EQ:
            case SW:
            case LIKE:
            default:
                result = getCastedExpression(expression).eq(castedValue);
                break;
        }
        return result;
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
