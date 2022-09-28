package de.domschmidt.datatable.factory;

import com.querydsl.core.types.OrderSpecifier;
import de.domschmidt.datatable.dto.query.DataQueryColumnSortDirDto;
import de.domschmidt.datatable.dto.query.DataQueryColumnSpecDto;
import de.domschmidt.datatable.dto.query.DataQuerySpecDto;
import de.domschmidt.datatable.factory.data.ColumnUageConfiguration;
import de.domschmidt.datatable.factory.data.ColumnUsageDescription;
import de.domschmidt.datatable.factory.exception.DataTableQueryException;
import de.domschmidt.datatable.factory.jpa_search.ColumnExpressionTypeFactory;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.util.HashMap;
import java.util.Map;

public class DataTableOrderByFactory {

    public OrderSpecifier<?>[] buildOrderBySpecifiers(
            final ColumnUageConfiguration columnUageConfiguration,
            final DataQuerySpecDto querySpec
    ) {
        final Map<Integer, OrderSpecifier<?>> results = new HashMap<>();

        if (querySpec != null && querySpec.getColumnSpecByColumnId() != null) {
            for (final Map.Entry<String, DataQueryColumnSpecDto> dataQueryColumnSpecDtoEntry : querySpec.getColumnSpecByColumnId().entrySet()) {
                final DataQueryColumnSortDirDto rawSortBy = dataQueryColumnSpecDtoEntry.getValue().getSortDir();
                final Integer rawSortIdx = dataQueryColumnSpecDtoEntry.getValue().getSortIdx();
                if (rawSortBy != null && rawSortIdx != null) {
                    final ColumnUsageDescription<?> columnDescription =
                            columnUageConfiguration.getColumnDescriptionIndexedById().get(dataQueryColumnSpecDtoEntry.getKey());
                    if (columnDescription != null) {
                        final IExpressionSearch<?> iExpressionSearchProvider =
                                ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());
                        try {
                            results.put(rawSortIdx, iExpressionSearchProvider.createOrderBySpecifier(
                                    columnDescription.getExpression(),
                                    DataQueryColumnSortDirDto.ASC.equals(rawSortBy)
                            ));
                        } catch (final DataTableUnsupportedExpressionTypeException e) {
                            throw new DataTableQueryException("Invalid search query", e);
                        }
                    } else {
                        throw new DataTableQueryException("Invalid search query");
                    }
                }
            }
        } else {
            // default query spec
            for (final ColumnUsageDescription<?> columnDescription : columnUageConfiguration.getColumnDescriptions()) {
                if (columnDescription.getDefaultSortDir() != null && columnDescription.getDefaultSortIdx() != null) {
                    final IExpressionSearch<?> iExpressionSearchProvider =
                            ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());

                    try {
                        results.put(columnDescription.getDefaultSortIdx(), iExpressionSearchProvider.createOrderBySpecifier(
                                columnDescription.getExpression(),
                                DataQueryColumnSortDirDto.ASC.equals(columnDescription.getDefaultSortDir())
                        ));
                    } catch (final DataTableUnsupportedExpressionTypeException e) {
                        throw new DataTableQueryException("Invalid search query", e);
                    }
                }
            }
        }

        if (results.isEmpty()) {
            for (ColumnUsageDescription<?> columnDescription : columnUageConfiguration.getColumnDescriptions()) {
                final DataQueryColumnSortDirDto defaultSortDir = columnDescription.getDefaultSortDir();
                final Integer defaultSortIdx = columnDescription.getDefaultSortIdx();
                if (defaultSortDir != null) {
                    final IExpressionSearch<?> iExpressionSearchProvider =
                            ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());
                    try {
                        results.put(defaultSortIdx, iExpressionSearchProvider.createOrderBySpecifier(
                                columnDescription.getExpression(),
                                DataQueryColumnSortDirDto.ASC.equals(defaultSortDir)
                        ));
                    } catch (final DataTableUnsupportedExpressionTypeException e) {
                        throw new DataTableQueryException("Invalid search query", e);
                    }
                }
            }
        }

        return results.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue).toArray(OrderSpecifier[]::new);
    }
}
