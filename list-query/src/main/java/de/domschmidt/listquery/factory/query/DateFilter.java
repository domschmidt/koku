package de.domschmidt.listquery.factory.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringExpressions;
import de.domschmidt.listquery.dto.request.EnumSearchOperator;
import de.domschmidt.listquery.dto.request.EnumSearchOperatorHint;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFilter implements IListFilter {

    @Override
    public BooleanExpression buildGlobalSearchExpression(final Expression<?> expr, final String query) {
        if (query == null || query.isEmpty() || !(expr instanceof DateExpression<?> dateExpression)) {
            return null;
        }

        return formattedDateExpression(dateExpression).like('%' + query + '%');
    }

    @Override
    public BooleanExpression buildSearchExpression(final Expression<?> expr, final QueryPredicate query) {
        if (query == null
                || query.getSearchExpression() == null
                || query.getSearchExpression().isEmpty()
                || !(expr instanceof DateExpression<?> dateExpression)) {
            return null;
        }

        final DateExpression<LocalDate> castedExpr = castDateExpression(dateExpression);
        final String rawSearchExpr = query.getSearchExpression();
        final LocalDate searchExpression = LocalDate.parse(rawSearchExpr, DateTimeFormatter.ISO_DATE);
        final BooleanExpression result = buildSearchExpression(castedExpr, query, rawSearchExpr, searchExpression);
        return Boolean.TRUE.equals(query.getNegate()) && result != null ? result.not() : result;
    }

    private BooleanExpression buildSearchExpression(
            final DateExpression<LocalDate> castedExpr,
            final QueryPredicate query,
            final String rawSearchExpr,
            final LocalDate searchExpression) {
        final EnumSearchOperator operator = query.getSearchOperator();
        if (usesYearlyRecurringComparison(query, operator)) {
            return buildYearlyRecurringSearchExpression(castedExpr, searchExpression, operator);
        }
        return switch (operator) {
            case LIKE, EQ -> castedExpr.eq(searchExpression);
            case LESS -> castedExpr.lt(searchExpression);
            case GREATER -> castedExpr.gt(searchExpression);
            case LESS_OR_EQ -> castedExpr.loe(searchExpression);
            case GREATER_OR_EQ -> castedExpr.goe(searchExpression);
            case STARTS_WITH -> formattedDateExpression(castedExpr).startsWithIgnoreCase(rawSearchExpr);
            case ENDS_WITH -> formattedDateExpression(castedExpr).endsWithIgnoreCase(rawSearchExpr);
        };
    }

    private BooleanExpression buildYearlyRecurringSearchExpression(
            final DateExpression<LocalDate> castedExpr,
            final LocalDate searchExpression,
            final EnumSearchOperator operator) {
        final BooleanExpression monthBeforeOrAfter =
                switch (operator) {
                    case LIKE, EQ, LESS, LESS_OR_EQ -> castedExpr.month().lt(searchExpression.getMonthValue());
                    case GREATER, GREATER_OR_EQ -> castedExpr.month().gt(searchExpression.getMonthValue());
                    default ->
                        throw new IllegalArgumentException("Unsupported yearly recurring date operator: " + operator);
                };
        final BooleanExpression dayComparison =
                switch (operator) {
                    case LIKE, EQ -> castedExpr.dayOfMonth().eq(searchExpression.getDayOfMonth());
                    case LESS -> castedExpr.dayOfMonth().lt(searchExpression.getDayOfMonth());
                    case GREATER -> castedExpr.dayOfMonth().gt(searchExpression.getDayOfMonth());
                    case LESS_OR_EQ -> castedExpr.dayOfMonth().loe(searchExpression.getDayOfMonth());
                    case GREATER_OR_EQ -> castedExpr.dayOfMonth().goe(searchExpression.getDayOfMonth());
                    default ->
                        throw new IllegalArgumentException("Unsupported yearly recurring date operator: " + operator);
                };

        return castedExpr
                .year()
                .loe(searchExpression.getYear())
                .and(monthBeforeOrAfter.or(
                        castedExpr.month().eq(searchExpression.getMonthValue()).and(dayComparison)));
    }

    private static boolean usesYearlyRecurringComparison(
            final QueryPredicate query, final EnumSearchOperator operator) {
        return EnumSearchOperatorHint.YEARLY_RECURRING.equals(query.getSearchOperatorHint())
                && operator != EnumSearchOperator.STARTS_WITH
                && operator != EnumSearchOperator.ENDS_WITH;
    }

    private static StringExpression formattedDateExpression(final DateExpression<?> castedExpr) {
        return StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0')
                .append(".")
                .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0'))
                .append(".")
                .append(StringExpressions.lpad(castedExpr.year().stringValue(), 4, '0'));
    }

    private static DateExpression<LocalDate> castDateExpression(final DateExpression<?> dateExpression) {
        return (DateExpression<LocalDate>) dateExpression;
    }
}
