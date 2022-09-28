package de.domschmidt.datatable.factory.jpa_search;

import com.querydsl.core.types.Expression;
import de.domschmidt.datatable.factory.exception.DataTableException;
import de.domschmidt.datatable.factory.jpa_search.types.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ColumnExpressionTypeFactory {

    private static final Map<Class<?>, IExpressionSearch<?>> types;

    static {
        final Map<Class<?>, IExpressionSearch<?>> newTypeMap = new HashMap<>();
        newTypeMap.put(String.class, new StringPathExprBuilder());
        newTypeMap.put(UUID.class, new UUIDPathExprBuilder());
        newTypeMap.put(Integer.class, new IntegerPathExprBuilder());
        newTypeMap.put(Long.class, new LongPathExprBuilder());
        newTypeMap.put(BigDecimal.class, new BigDecimalPathExprBuilder());
        newTypeMap.put(Enum.class, new EnumPathExprBuilder());
        newTypeMap.put(LocalDate.class, new LocalDatePathExprBuilder());
        newTypeMap.put(LocalDateTime.class, new LocalDateTimePathExprBuilder());
        newTypeMap.put(Year.class, new YearPathExprBuilder());
        newTypeMap.put(Boolean.class, new BooleanPathExprBuilder());
        newTypeMap.put(MonthDay.class, new MonthDayPathExprBuilder());

        types = Collections.unmodifiableMap(newTypeMap);
    }

    public static IExpressionSearch<?> getByExpression(final Expression<?> expression) {
        if (expression.getType().isEnum()) {
            return types.get(Enum.class);
        }
        final IExpressionSearch<?> expressionProviderImpl = types.get(expression.getType());
        if (expressionProviderImpl == null) {
            throw new DataTableException("Missing column expression search provider for \"" + expression + "\"");
        }
        return expressionProviderImpl;
    }
}
