package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumExpression;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.util.Arrays;
import java.util.List;

public class EnumPathExprBuilder implements IExpressionSearch<EnumExpression> {
    @Override
    public EnumExpression getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!EnumExpression.class.isAssignableFrom(expression.getClass())) {
            throw new DataTableUnsupportedExpressionTypeException("EnumExpression is not assignable to " + expression.getClass());
        }
        return (EnumExpression) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        final DataQueryColumnOPDto op;
        if (customOp == null) {
            // default
            op = DataQueryColumnOPDto.EQ;
        } else {
            op = customOp;
        }

        final String castedValue;
        final List<Enum> compareValues;
        try {
            castedValue = ((String) rawSearchValue).trim();
            compareValues = Arrays.asList(Enum.valueOf(((EnumExpression) expression).getType(), castedValue));
            if (compareValues == null || compareValues.isEmpty()) {
                throw new DataTableUnsupportedExpressionTypeException("Cannot extract Enum of given search value \"" + rawSearchValue + "\"");
            }
        } catch (final ClassCastException e) {
            throw new DataTableUnsupportedExpressionTypeException("Cannot extract Enum of given search value \"" + rawSearchValue + "\"", e);
        }

        BooleanExpression finalResult = null;

        for (final Enum compareValue : compareValues) {
            final BooleanExpression result;
            switch (op) {
                case LT:
                    result = getCastedExpression(expression).lt(compareValue);
                    break;
                case LOE:
                    result = getCastedExpression(expression).loe(compareValue);
                    break;
                case GT:
                    result = getCastedExpression(expression).gt(compareValue);
                    break;
                case GOE:
                    result = getCastedExpression(expression).goe(compareValue);
                    break;
                case LIKE:
                case SW:
                case EW:
                case EQ:
                default:
                    result = getCastedExpression(expression).eq(compareValue);
                    break;
            }
            if (finalResult == null) {
                finalResult = result;
            } else {
                finalResult = finalResult.or(result);
            }
        }
        return finalResult;
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
