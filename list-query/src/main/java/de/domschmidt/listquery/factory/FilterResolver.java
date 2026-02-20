package de.domschmidt.listquery.factory;

import de.domschmidt.listquery.factory.query.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FilterResolver {

    static final Map<Class<?>, IListFilter> FILTERS;

    static {
        final Map<Class<?>, IListFilter> newFilters = new HashMap<>();
        newFilters.put(String.class, new StringFilter());
        newFilters.put(LocalDateTime.class, new DateTimeFilter());
        newFilters.put(LocalDate.class, new DateFilter());
        newFilters.put(LocalTime.class, new TimeFilter());
        newFilters.put(MonthDay.class, new MonthDayFilter());
        newFilters.put(Long.class, new LongFilter());
        newFilters.put(UUID.class, new UUIDFilter());
        newFilters.put(Boolean.class, new BooleanFilter());
        FILTERS = newFilters;
    }

    public static IListFilter resolveFilter(final Class<?> clazz) {
        return FILTERS.get(clazz);
    }
}
