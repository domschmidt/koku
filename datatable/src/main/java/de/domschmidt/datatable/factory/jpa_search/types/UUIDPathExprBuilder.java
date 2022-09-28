package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.Expressions;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.util.UUID;

public class UUIDPathExprBuilder implements IExpressionSearch<ComparablePath<UUID>> {

    @Override
    public ComparablePath<UUID> getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!ComparablePath.class.isAssignableFrom(expression.getClass()) || !expression.getType().equals(UUID.class)) {
            throw new DataTableUnsupportedExpressionTypeException("ComparablePath is not assignable to " + expression.getClass());
        }
        return (ComparablePath<UUID>) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        final String castedValue;
        try {
            castedValue = (String) rawSearchValue;
        } catch (final ClassCastException e) {
            throw new DataTableUnsupportedExpressionTypeException(rawSearchValue + " cannot be casted to String");
        }
        final BooleanExpression result;

        final DataQueryColumnOPDto op;
        if (customOp == null) {
            // default
            op = DataQueryColumnOPDto.LIKE;
        } else {
            op = customOp;
        }

        switch (op) {
            case LT:
                result = Expressions.booleanOperation(
                        Ops.LT,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue.toLowerCase()));
                break;
            case LOE:
                result = Expressions.booleanOperation(
                        Ops.LOE,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue.toLowerCase()));
                break;
            case GT:
                result = Expressions.booleanOperation(
                        Ops.GT,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue.toLowerCase()));
                break;
            case GOE:
                result = Expressions.booleanOperation(
                        Ops.GOE,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue.toLowerCase()));
                break;
            case EW:
                result = Expressions.booleanOperation(
                        Ops.ENDS_WITH_IC,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue));
                break;
            case EQ:
                result = Expressions.booleanOperation(
                        Ops.EQ_IGNORE_CASE,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue));
                break;
            case SW:
                result = Expressions.booleanOperation(
                        Ops.STARTS_WITH_IC,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create(castedValue));
                break;
            case LIKE:
            default:
                result = Expressions.booleanOperation(
                        Ops.LIKE_IC,
                        Expressions.stringOperation(Ops.STRING_CAST, getCastedExpression(expression)).toLowerCase(),
                        ConstantImpl.create("%" + castedValue + "%"));
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
