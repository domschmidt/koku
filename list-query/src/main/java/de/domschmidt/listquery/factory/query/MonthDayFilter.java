package de.domschmidt.listquery.factory.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.StringExpressions;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class MonthDayFilter implements IListFilter {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendLiteral("--")
                    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                    .appendLiteral('-')
                    .appendValue(ChronoField.DAY_OF_MONTH, 2)
                    .toFormatter();

    @Override
    public BooleanExpression buildGlobalSearchExpression(final Expression<?> expr, final String query) {
        if (query == null || query.isEmpty() || !(expr instanceof DateExpression castedExpr)) {
            return null;
        }

        return StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0').append(".")
                .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0'))
                .like('%' + query + '%');
    }

    @Override
    public BooleanExpression buildSearchExpression(final Expression<?> expr, final QueryPredicate query) {
        if (query == null || query.getSearchExpression() == null || query.getSearchExpression().isEmpty() || !(expr instanceof DateExpression castedExpr)) {
            return null;
        }

        final String rawSearchExpr = query.getSearchExpression();
        final MonthDay searchExpression = MonthDay.parse(rawSearchExpr, MONTH_DAY_FORMATTER);
        BooleanExpression result = null;
        switch (query.getSearchOperator()) {
            case LIKE, EQ -> result = castedExpr.eq(searchExpression);
            case LESS -> result = castedExpr.lt(searchExpression);
            case GREATER -> result = castedExpr.gt(searchExpression);
            case LESS_OR_EQ -> result = castedExpr.loe(searchExpression);
            case GREATER_OR_EQ -> result = castedExpr.goe(searchExpression);
            case STARTS_WITH -> result = StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0').append(".")
                    .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0')).startsWithIgnoreCase(rawSearchExpr);
            case ENDS_WITH -> result = StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0').append(".")
                    .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0')).endsWithIgnoreCase(rawSearchExpr);
        }
        if (result != null && Boolean.TRUE.equals(query.getNegate())) {
            result = result.not();
        }
        return result;
    }
}
