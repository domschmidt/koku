package de.domschmidt.datatable.factory.data;


import com.querydsl.core.types.Expression;
import de.domschmidt.datatable.dto.query.DataQueryColumnSortDirDto;
import lombok.Getter;

import java.util.Set;

@Getter
public class ColumnUsageDescription<T> {

    private final String id;
    private final Expression<T> expression;
    private final String columnName;
    private final DataQueryColumnSortDirDto defaultSortDir;
    private final Integer defaultSortIdx;
    private final T defaultSearchValue;
    private final T summary;
    private final Boolean hidden;
    private final Set<T> possibleSelectValues;

    ColumnUsageDescription(
            final ColumnUsageDescriptionBuilder<T> builder
    ) {
        this.id = builder.getId();
        this.columnName = builder.getColumnName();
        this.expression = builder.getExpression();
        this.defaultSortDir = builder.getDefaultSortDir();
        this.defaultSortIdx = builder.getDefaultSortIdx();
        this.defaultSearchValue = builder.getDefaultSearchValue();
        this.summary = builder.getSummary();
        this.hidden = builder.getHidden();
        this.possibleSelectValues = builder.getPossibleSelectValues();
    }

}
