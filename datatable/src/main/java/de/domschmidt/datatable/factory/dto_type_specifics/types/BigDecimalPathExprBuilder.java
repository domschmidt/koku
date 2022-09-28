package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.math.BigDecimal;

public class BigDecimalPathExprBuilder implements IExpressionSearch<NumberExpression<BigDecimal>> {

    @Override
    public NumberExpression<BigDecimal> getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!NumberExpression.class.isAssignableFrom(expression.getClass()) || !expression.getType().equals(BigDecimal.class)) {
            throw new DataTableUnsupportedExpressionTypeException("NumberExpression is not assignable to " + expression.getClass());
        }
        return (NumberExpression<BigDecimal>) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        final BigDecimal castedValue;
        try {
            castedValue = new BigDecimal(rawSearchValue.toString());
        } catch (final NumberFormatException e) {
            throw new DataTableUnsupportedExpressionTypeException("Cannot extract BigDecimal of given search value \"" + rawSearchValue + "\"");
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
            case LIKE:
                result = getCastedExpression(expression).like('%' + String.valueOf(castedValue) + '%');
                break;
            case SW:
                result = getCastedExpression(expression).stringValue().trim().startsWithIgnoreCase(String.valueOf(castedValue));
                break;
            case EW:
                result = getCastedExpression(expression).stringValue().trim().endsWithIgnoreCase(String.valueOf(castedValue));
                break;
            case EQ:
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
