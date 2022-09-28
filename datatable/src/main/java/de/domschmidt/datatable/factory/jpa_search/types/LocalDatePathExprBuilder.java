package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDatePathExprBuilder implements IExpressionSearch<StringExpression> {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern(
            "[dd.MM.yyyy][yyyy-MM-dd]"
    );

    @Override
    public StringExpression getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!StringExpression.class.isAssignableFrom(expression.getClass()) || !expression.getType().equals(String.class)) {
            throw new DataTableUnsupportedExpressionTypeException("StringExpression is not assignable to " + expression.getClass());
        }
        return (StringExpression) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        final LocalDate castedValue;
        try {
            castedValue = LocalDate.parse((String) rawSearchValue, DATE_PATTERN);
        } catch (final DateTimeParseException e) {
            throw new DataTableUnsupportedExpressionTypeException(rawSearchValue + " cannot be converted to LocalDate");
        }
        final BooleanExpression result = null;

        final DataQueryColumnOPDto op;
        if (customOp == null) {
            // default
            op = DataQueryColumnOPDto.LIKE;
        } else {
            op = customOp;
        }

//        switch (op) {
//            case LT:
//                result = getCastedExpression(expression).lt(castedValue);
//                break;
//            case LOE:
//                result = getCastedExpression(expression).loe(castedValue);
//                break;
//            case GT:
//                result = getCastedExpression(expression).trim().gt(castedValue);
//                break;
//            case GOE:
//                result = getCastedExpression(expression).trim().goe(castedValue);
//                break;
//            case EW:
//                result = getCastedExpression(expression).trim().endsWithIgnoreCase(castedValue);
//                break;
//            case EQ:
//                result = getCastedExpression(expression).trim().equalsIgnoreCase(castedValue);
//                break;
//            case SW:
//                result = getCastedExpression(expression).trim().startsWithIgnoreCase(castedValue);
//                break;
//            case LIKE:
//            default:
//                result = getCastedExpression(expression).trim().likeIgnoreCase('%' + castedValue + '%');
//                break;
//        }
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
