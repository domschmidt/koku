package de.domschmidt.datatable.factory.jpa_search.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimePathExprBuilder implements IExpressionSearch<DateTimeExpression<LocalDateTime>> {

    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern(
            "[[dd.MM.yyyy]['T'][HH:mm[:ss]]][[yyyy-MM-dd]['T'][HH:mm[:ss]]]"
    );

    @Override
    public DateTimeExpression<LocalDateTime> getCastedExpression(
            final Expression<?> expression
    ) throws DataTableUnsupportedExpressionTypeException {
        if (!DateTimePath.class.isAssignableFrom(expression.getClass()) || !expression.getType().equals(LocalDateTime.class)) {
            throw new DataTableUnsupportedExpressionTypeException("StringExpression is not assignable to " + expression.getClass());
        }
        return (DateTimeExpression<LocalDateTime>) expression;
    }

    @Override
    public BooleanExpression createExpression(
            final Expression<?> expression,
            final Object rawSearchValue,
            final DataQueryColumnOPDto customOp
    ) throws DataTableUnsupportedExpressionTypeException {
        LocalDate castedDate;
        try {
            castedDate = LocalDate.parse((String) rawSearchValue, DATE_TIME_PATTERN);
        } catch (final DateTimeParseException e) {
            castedDate = null;
        }
        LocalTime castedTime;
        try {
            castedTime = LocalTime.parse((String) rawSearchValue, DATE_TIME_PATTERN);
        } catch (final DateTimeParseException e) {
            castedTime = null;
        }

        if (castedDate == null && castedTime == null) {
            throw new DataTableUnsupportedExpressionTypeException(rawSearchValue + " cannot be converted to LocalDate nor LocalTime");
        }

        BooleanExpression result = null;

        final DataQueryColumnOPDto op;
        if (customOp == null) {
            // default
            op = DataQueryColumnOPDto.LIKE;
        } else {
            op = customOp;
        }

        final DateTimeExpression<LocalDateTime> castedExpression = getCastedExpression(expression);

        switch (op) {
            case LT:
                if (castedTime != null) {
                    result = castedExpression.hour().lt(castedTime.getHour())
                            .or(castedExpression.hour().eq(castedTime.getHour())
                                    .and(castedExpression.minute().lt(castedTime.getMinute()))
                            );
                }
                if (castedDate != null) {
                    result = castedExpression.year().lt(castedDate.getYear())
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().lt(castedDate.getMonthValue()))
                            )
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                            .and(castedExpression.dayOfMonth().lt(castedDate.getDayOfMonth()))
                            )
                            .or(
                                    ExpressionUtils.and(
                                            castedExpression.year().eq(castedDate.getYear())
                                                    .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                                    .and(castedExpression.dayOfMonth().eq(castedDate.getDayOfMonth())),
                                            result
                                    )
                            );
                }
                break;
            case LOE:
            case EW:
                if (castedTime != null) {
                    result = castedExpression.hour().loe(castedTime.getHour())
                            .or(castedExpression.hour().eq(castedTime.getHour())
                                    .and(castedExpression.minute().loe(castedTime.getMinute()))
                            );
                }
                if (castedDate != null) {
                    result = castedExpression.year().lt(castedDate.getYear())
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().loe(castedDate.getMonthValue()))
                            )
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                            .and(castedExpression.dayOfMonth().loe(castedDate.getDayOfMonth()))
                            )
                            .or(
                                    ExpressionUtils.and(
                                            castedExpression.year().eq(castedDate.getYear())
                                                    .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                                    .and(castedExpression.dayOfMonth().eq(castedDate.getDayOfMonth())),
                                            result
                                    )
                            );
                }
                break;
            case GT:
                if (castedTime != null) {
                    result = castedExpression.hour().gt(castedTime.getHour())
                            .or(castedExpression.hour().eq(castedTime.getHour())
                                    .and(castedExpression.minute().gt(castedTime.getMinute()))
                            );
                }
                if (castedDate != null) {
                    result = castedExpression.year().gt(castedDate.getYear())
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().gt(castedDate.getMonthValue()))
                            )
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                            .and(castedExpression.dayOfMonth().gt(castedDate.getDayOfMonth()))
                            )
                            .or(
                                    ExpressionUtils.and(
                                            castedExpression.year().eq(castedDate.getYear())
                                                    .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                                    .and(castedExpression.dayOfMonth().eq(castedDate.getDayOfMonth())),
                                            result
                                    )
                            );
                }
                break;
            case SW:
            case GOE:
                if (castedTime != null) {
                    result = castedExpression.hour().gt(castedTime.getHour())
                            .or(castedExpression.hour().eq(castedTime.getHour())
                                    .and(castedExpression.minute().goe(castedTime.getMinute()))
                            );
                }
                if (castedDate != null) {
                    result = castedExpression.year().gt(castedDate.getYear())
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().gt(castedDate.getMonthValue()))
                            )
                            .or(
                                    castedExpression.year().eq(castedDate.getYear())
                                            .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                            .and(castedExpression.dayOfMonth().gt(castedDate.getDayOfMonth()))
                            )
                            .or(
                                    ExpressionUtils.and(
                                            castedExpression.year().eq(castedDate.getYear())
                                                    .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                                    .and(castedExpression.dayOfMonth().eq(castedDate.getDayOfMonth())),
                                            result
                                    )
                            );
                }
                break;
            case EQ:
            case LIKE:
            default:
                if (castedTime != null) {
                    result = castedExpression.hour().eq(castedTime.getHour())
                            .and(castedExpression.minute().eq(castedTime.getMinute()));
                }
                if (castedDate != null) {
                    if (result != null) {
                        result = castedExpression.year().eq(castedDate.getYear())
                                .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                .and(castedExpression.dayOfMonth().eq(castedDate.getDayOfMonth()))
                                .and(result);
                    } else {
                        result = castedExpression.year().eq(castedDate.getYear())
                                .and(castedExpression.month().eq(castedDate.getMonthValue()))
                                .and(castedExpression.dayOfMonth().eq(castedDate.getDayOfMonth()));
                    }
                }
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
