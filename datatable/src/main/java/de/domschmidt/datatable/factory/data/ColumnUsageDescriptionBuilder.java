package de.domschmidt.datatable.factory.data;

import com.querydsl.core.types.Expression;
import de.domschmidt.datatable.dto.query.DataQueryColumnSortDirDto;
import lombok.Getter;

import java.util.Set;

@Getter
public class ColumnUsageDescriptionBuilder<T> {

    private final String id;
    private final Expression<T> expression;
    private final String columnName;
    private DataQueryColumnSortDirDto defaultSortDir;
    private Integer defaultSortIdx;
    private T defaultSearchValue;
    private T summary;
    private Boolean hidden;
    private Set<T> possibleSelectValues;

    public ColumnUsageDescriptionBuilder(
            final String id,
            final String columnName,
            final Expression<T> expression
    ) {
        this.id = id;
        this.columnName = columnName;
        this.expression = expression;
    }

    public ColumnUsageDescriptionBuilder<T> defaultSort(
            final DataQueryColumnSortDirDto defaultSortDir,
            final Integer defaultSortIdx
    ) {
        this.defaultSortDir = defaultSortDir;
        this.defaultSortIdx = defaultSortIdx;
        return this;
    }

    public ColumnUsageDescriptionBuilder<T> defaultSearchValue(
            final T defaultSearchValue
    ) {
        this.defaultSearchValue = defaultSearchValue;
        return this;
    }

    public ColumnUsageDescriptionBuilder<T> summary(
            final T summary
    ) {
        this.summary = summary;
        return this;
    }

    public ColumnUsageDescriptionBuilder<T> hidden(
            final Boolean hidden
    ) {
        this.hidden = hidden;
        return this;
    }

    public ColumnUsageDescriptionBuilder<T> possibleSelectValues(
            final Set<T> possibleSelectValues
    ) {
        this.possibleSelectValues = possibleSelectValues;
        return this;
    }

    public ColumnUsageDescription<T> build() {
        return new ColumnUsageDescription<>(this);
    }

}
