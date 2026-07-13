package de.domschmidt.listquery.factory.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import de.domschmidt.listquery.dto.request.EnumSearchOperator;
import de.domschmidt.listquery.dto.request.EnumSearchOperatorHint;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.factory.IListFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class FilterContractTest {

    @ParameterizedTest
    @MethodSource("filters")
    void rejectsMissingQueriesAndIncompatibleExpressions(
            final IListFilter filter, final Expression<?> compatibleExpression, final String value) {
        assertThat(filter.buildGlobalSearchExpression(compatibleExpression, null))
                .isNull();
        assertThat(filter.buildGlobalSearchExpression(compatibleExpression, "")).isNull();
        assertThat(filter.buildSearchExpression(compatibleExpression, null)).isNull();
        assertThat(filter.buildSearchExpression(compatibleExpression, predicate("", EnumSearchOperator.EQ)))
                .isNull();
        assertThat(filter.buildSearchExpression(
                        Expressions.constant("wrongType"), predicate(value, EnumSearchOperator.EQ)))
                .isNull();
    }

    @ParameterizedTest
    @MethodSource("filters")
    void buildsGlobalSearchAndSupportsNegation(
            final IListFilter filter, final Expression<?> expression, final String value) {
        assertThat(filter.buildGlobalSearchExpression(expression, value)).isNotNull();

        final BooleanExpression positive =
                filter.buildSearchExpression(expression, predicate(value, EnumSearchOperator.EQ));
        final QueryPredicate negatedPredicate = predicate(value, EnumSearchOperator.EQ);
        negatedPredicate.setNegate(true);
        final BooleanExpression negated = filter.buildSearchExpression(expression, negatedPredicate);

        assertThat(positive).isNotNull();
        assertThat(negated).isNotNull();
        assertThat(negated.toString()).isNotEqualTo(positive.toString()).contains("!");
    }

    @ParameterizedTest
    @EnumSource(EnumSearchOperator.class)
    void stringFilterSupportsEveryOperator(final EnumSearchOperator operator) {
        assertThat(new StringFilter()
                        .buildSearchExpression(Expressions.stringPath("name"), predicate("Example", operator)))
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(EnumSearchOperator.class)
    void longFilterSupportsEveryOperator(final EnumSearchOperator operator) {
        assertThat(new LongFilter()
                        .buildSearchExpression(Expressions.numberPath(Long.class, "amount"), predicate("42", operator)))
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(EnumSearchOperator.class)
    void booleanFilterSupportsEveryOperator(final EnumSearchOperator operator) {
        assertThat(new BooleanFilter()
                        .buildSearchExpression(Expressions.booleanPath("deleted"), predicate("true", operator)))
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(EnumSearchOperator.class)
    void temporalFiltersSupportEveryOperator(final EnumSearchOperator operator) {
        assertThat(new DateFilter()
                        .buildSearchExpression(
                                Expressions.datePath(LocalDate.class, "date"), predicate("2026-07-12", operator)))
                .isNotNull();
        assertThat(new DateTimeFilter()
                        .buildSearchExpression(
                                Expressions.dateTimePath(LocalDateTime.class, "start"),
                                predicate("12.07.2026 14:30", operator)))
                .isNotNull();
        assertThat(new TimeFilter()
                        .buildSearchExpression(
                                Expressions.timePath(LocalTime.class, "time"), predicate("14:30", operator)))
                .isNotNull();
    }

    @ParameterizedTest
    @EnumSource(EnumSearchOperator.class)
    void monthDayFilterSupportsEveryOperatorAndNegation(final EnumSearchOperator operator) {
        final MonthDayFilter filter = new MonthDayFilter();
        final var expression = Expressions.datePath(MonthDay.class, "birthday");
        final QueryPredicate positive = predicate("--07-13", operator);
        final QueryPredicate negated = predicate("--07-13", operator);
        negated.setNegate(true);

        assertThat(filter.buildSearchExpression(expression, positive)).isNotNull();
        assertThat(filter.buildSearchExpression(expression, negated)).isNotNull();
        assertThat(filter.buildGlobalSearchExpression(expression, "13.07")).isNotNull();
    }

    @Test
    void monthDayFilterRejectsMissingAndIncompatibleInput() {
        final MonthDayFilter filter = new MonthDayFilter();
        final var expression = Expressions.datePath(MonthDay.class, "birthday");

        assertThat(filter.buildGlobalSearchExpression(expression, null)).isNull();
        assertThat(filter.buildGlobalSearchExpression(Expressions.stringPath("name"), "13.07"))
                .isNull();
        assertThat(filter.buildSearchExpression(expression, null)).isNull();
        assertThat(filter.buildSearchExpression(expression, predicate("", EnumSearchOperator.EQ)))
                .isNull();
    }

    @ParameterizedTest
    @EnumSource(EnumSearchOperator.class)
    void uuidFilterSupportsEveryOperator(final EnumSearchOperator operator) {
        assertThat(new UUIDFilter()
                        .buildSearchExpression(
                                Expressions.comparablePath(UUID.class, "id"),
                                predicate("bb698c67-a869-445a-89ea-eb4fc3bcae64", operator)))
                .isNotNull();
    }

    @Test
    void yearlyRecurringDateComparisonIgnoresLaterYearsAndComparesMonthAndDay() {
        final QueryPredicate predicate = predicate("2026-07-12", EnumSearchOperator.LESS_OR_EQ);
        predicate.setSearchOperatorHint(EnumSearchOperatorHint.YEARLY_RECURRING);

        final String expression = new DateFilter()
                .buildSearchExpression(Expressions.datePath(LocalDate.class, "birthday"), predicate)
                .toString();

        assertThat(expression).contains("year(birthday) <= 2026", "month(birthday)", "dayofmonth(birthday)");
    }

    @ParameterizedTest
    @EnumSource(
            value = EnumSearchOperator.class,
            names = {"LIKE", "EQ", "LESS", "GREATER", "LESS_OR_EQ", "GREATER_OR_EQ"})
    void yearlyRecurringDateSupportsComparisonOperators(final EnumSearchOperator operator) {
        final QueryPredicate predicate = predicate("2026-07-12", operator);
        predicate.setSearchOperatorHint(EnumSearchOperatorHint.YEARLY_RECURRING);

        assertThat(new DateFilter().buildSearchExpression(Expressions.datePath(LocalDate.class, "birthday"), predicate))
                .isNotNull();
    }

    private static QueryPredicate predicate(final String value, final EnumSearchOperator operator) {
        return QueryPredicate.builder()
                .searchExpression(value)
                .searchOperator(operator)
                .build();
    }

    private static Stream<Arguments> filters() {
        return Stream.of(
                Arguments.of(new StringFilter(), Expressions.stringPath("name"), "Example"),
                Arguments.of(new LongFilter(), Expressions.numberPath(Long.class, "amount"), "42"),
                Arguments.of(new BooleanFilter(), Expressions.booleanPath("deleted"), "true"),
                Arguments.of(new DateFilter(), Expressions.datePath(LocalDate.class, "date"), "2026-07-12"),
                Arguments.of(
                        new DateTimeFilter(),
                        Expressions.dateTimePath(LocalDateTime.class, "start"),
                        "12.07.2026 14:30"),
                Arguments.of(new TimeFilter(), Expressions.timePath(LocalTime.class, "time"), "14:30"),
                Arguments.of(
                        new UUIDFilter(),
                        Expressions.comparablePath(UUID.class, "id"),
                        "bb698c67-a869-445a-89ea-eb4fc3bcae64"));
    }
}
