package de.domschmidt.listquery.factory.query;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.StringExpressions;
import de.domschmidt.listquery.dto.request.EnumSearchOperatorHint;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFilter implements IListFilter {

    @Override
    public BooleanExpression buildGlobalSearchExpression(final Expression<?> expr, final String query) {
        if (query == null || query.isEmpty() || !(expr instanceof DateExpression castedExpr)) {
            return null;
        }

        return StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0').append(".")
                .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0')).append(".")
                .append(StringExpressions.lpad(castedExpr.year().stringValue(), 4, '0'))
                .like('%' + query + '%');
    }

    @Override
    public BooleanExpression buildSearchExpression(final Expression<?> expr, final QueryPredicate query) {
        if (query == null || query.getSearchExpression() == null || query.getSearchExpression().isEmpty() || !(expr instanceof DateExpression castedExpr)) {
            return null;
        }

        final String rawSearchExpr = query.getSearchExpression();
        final LocalDate searchExpression = LocalDate.parse(rawSearchExpr, DateTimeFormatter.ISO_DATE);
        BooleanExpression result = null;
        switch (query.getSearchOperator()) {
            case LIKE, EQ -> {
                if (EnumSearchOperatorHint.YEARLY_RECURRING.equals(query.getSearchOperatorHint())) {
                    result = castedExpr.year().loe(searchExpression.getYear()).and(
                            castedExpr.month().lt(searchExpression.getMonthValue())
                                    .or(castedExpr.month().eq(searchExpression.getMonthValue())
                                            .and(castedExpr.dayOfMonth().eq(searchExpression.getDayOfMonth()))
                                    )
                    );
                } else {
                    result = castedExpr.eq(searchExpression);
                }
            }
            case LESS -> {
                if (EnumSearchOperatorHint.YEARLY_RECURRING.equals(query.getSearchOperatorHint())) {
                    result = castedExpr.year().loe(searchExpression.getYear()).and(
                            castedExpr.month().lt(searchExpression.getMonthValue())
                                    .or(castedExpr.month().eq(searchExpression.getMonthValue())
                                            .and(castedExpr.dayOfMonth().lt(searchExpression.getDayOfMonth()))
                                    )
                    );
                } else {
                    result = castedExpr.lt(searchExpression);
                }
            }
            case GREATER -> {
                if (EnumSearchOperatorHint.YEARLY_RECURRING.equals(query.getSearchOperatorHint())) {
                    result = castedExpr.year().loe(searchExpression.getYear()).and(
                            castedExpr.month().gt(searchExpression.getMonthValue())
                                    .or(castedExpr.month().eq(searchExpression.getMonthValue())
                                            .and(castedExpr.dayOfMonth().gt(searchExpression.getDayOfMonth()))
                                    )
                    );
                } else {
                    result = castedExpr.gt(searchExpression);
                }
            }
            case LESS_OR_EQ -> {
                if (EnumSearchOperatorHint.YEARLY_RECURRING.equals(query.getSearchOperatorHint())) {
                    result = castedExpr.year().loe(searchExpression.getYear()).and(
                            castedExpr.month().lt(searchExpression.getMonthValue())
                                    .or(castedExpr.month().eq(searchExpression.getMonthValue())
                                            .and(castedExpr.dayOfMonth().loe(searchExpression.getDayOfMonth()))
                                    )
                    );
                } else {
                    result = castedExpr.loe(searchExpression);
                }
            }
            case GREATER_OR_EQ -> {
                if (EnumSearchOperatorHint.YEARLY_RECURRING.equals(query.getSearchOperatorHint())) {
                    result = castedExpr.year().loe(searchExpression.getYear()).and(
                            castedExpr.month().gt(searchExpression.getMonthValue())
                                    .or(castedExpr.month().eq(searchExpression.getMonthValue())
                                            .and(castedExpr.dayOfMonth().goe(searchExpression.getDayOfMonth()))
                                    )
                    );
                } else {
                    result = castedExpr.goe(searchExpression);
                }
            }
            case STARTS_WITH -> result = StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0').append(".")
                    .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0')).append(".")
                    .append(StringExpressions.lpad(castedExpr.year().stringValue(), 4, '0')).startsWithIgnoreCase(rawSearchExpr);
            case ENDS_WITH -> result = StringExpressions.lpad(castedExpr.dayOfMonth().stringValue(), 2, '0').append(".")
                    .append(StringExpressions.lpad(castedExpr.month().stringValue(), 2, '0')).append(".")
                    .append(StringExpressions.lpad(castedExpr.year().stringValue(), 4, '0')).endsWithIgnoreCase(rawSearchExpr);
        }
        if (result != null && Boolean.TRUE.equals(query.getNegate())) {
            result = result.not();
        }
        return result;
    }
}
