package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.time.MonthDay;
import java.time.format.DateTimeParseException;

public class MonthDayPathExprBuilder implements IExpressionSearch<ComparableExpression<MonthDay>> {

    @Override
    public ComparableExpression<MonthDay> getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!ComparableExpression.class.isAssignableFrom(expression.getClass()) || !expression.getType().equals(MonthDay.class)) {
            throw new DataTableUnsupportedExpressionTypeException("ComparableExpression is not assignable to " + expression.getClass());
        }
        return (ComparableExpression<MonthDay>) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        final MonthDay castedValue;
        try {
            castedValue = MonthDay.parse(String.valueOf(rawSearchValue));
        } catch (final DateTimeParseException e) {
            throw new DataTableUnsupportedExpressionTypeException("Cannot extract MonthDay of given search value \"" + rawSearchValue + "\"", e);
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
