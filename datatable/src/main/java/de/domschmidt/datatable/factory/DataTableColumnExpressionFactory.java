package de.domschmidt.datatable.factory;

import com.querydsl.core.types.dsl.BooleanExpression;
import de.domschmidt.datatable.dto.query.DataQueryAdvancedSearchDto;
import de.domschmidt.datatable.dto.query.DataQueryColumnOPDto;
import de.domschmidt.datatable.dto.query.DataQueryColumnSpecDto;
import de.domschmidt.datatable.dto.query.DataQuerySpecDto;
import de.domschmidt.datatable.factory.data.ColumnUageConfiguration;
import de.domschmidt.datatable.factory.data.ColumnUsageDescription;
import de.domschmidt.datatable.factory.exception.DataTableQueryException;
import de.domschmidt.datatable.factory.jpa_search.ColumnExpressionTypeFactory;
import de.domschmidt.datatable.factory.jpa_search.IExpressionSearch;
import de.domschmidt.datatable.factory.jpa_search.exception.DataTableUnsupportedExpressionTypeException;

import java.util.List;
import java.util.Map;

public class DataTableColumnExpressionFactory {

    public BooleanExpression buildExpressions(
            final ColumnUageConfiguration columnUageConfiguration,
            final DataQuerySpecDto querySpec
    ) {
        BooleanExpression result = null;

        // build query by column
        if (querySpec != null && querySpec.getColumnSpecByColumnId() != null) {
            // custom column config has been applied
            for (final Map.Entry<String, DataQueryColumnSpecDto> dataQueryColumnSpecDtoEntry : querySpec.getColumnSpecByColumnId().entrySet()) {
                final DataQueryColumnSpecDto dataQueryColumnSpecDtoEntryValue = dataQueryColumnSpecDtoEntry.getValue();
                final ColumnUsageDescription<?> columnDescription = columnUageConfiguration.getColumnDescriptionIndexedById().get(dataQueryColumnSpecDtoEntry.getKey());
                final Object rawSearchValue = dataQueryColumnSpecDtoEntryValue.getSearch();
                final List<DataQueryAdvancedSearchDto> advancedSearchSpecs = dataQueryColumnSpecDtoEntryValue.getAdvancedSearchSpec();
                final List<Object> selectValues = dataQueryColumnSpecDtoEntryValue.getSelectValues();
                if (rawSearchValue != null) {
                    if (columnDescription != null) {
                        final BooleanExpression columnExpression;
                        final IExpressionSearch<?> iExpressionSearchProvider =
                                ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());

                        try {
                            columnExpression = iExpressionSearchProvider.createExpression(
                                    columnDescription.getExpression(),
                                    rawSearchValue,
                                    null
                            );
                        } catch (final DataTableUnsupportedExpressionTypeException e) {
                            throw new DataTableQueryException("Invalid search query", e);
                        }

                        if (result == null) {
                            result = columnExpression;
                        } else {
                            result = result.and(columnExpression);
                        }
                    } else {
                        throw new DataTableQueryException("Invalid search query");
                    }
                } else if (advancedSearchSpecs != null && !advancedSearchSpecs.isEmpty()) {
                    BooleanExpression advancedSearchSpecExpr = null;
                    for (final DataQueryAdvancedSearchDto advancedSearchSpec : advancedSearchSpecs) {
                        if (columnDescription != null) {
                            final BooleanExpression columnExpression;
                            final IExpressionSearch<?> iExpressionSearchProvider =
                                    ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());

                            try {
                                columnExpression = iExpressionSearchProvider.createExpression(
                                        columnDescription.getExpression(),
                                        advancedSearchSpec.getSearch(),
                                        advancedSearchSpec.getCustomOp()
                                );
                            } catch (DataTableUnsupportedExpressionTypeException e) {
                                throw new DataTableQueryException("Invalid search query", e);
                            }

                            if (advancedSearchSpecExpr == null) {
                                advancedSearchSpecExpr = columnExpression;
                            } else {
                                advancedSearchSpecExpr = advancedSearchSpecExpr.and(columnExpression);
                            }
                        } else {
                            throw new DataTableQueryException("Invalid search query");
                        }
                    }
                    if (advancedSearchSpecExpr != null) {
                        if (result == null) {
                            result = advancedSearchSpecExpr;
                        } else {
                            result = result.and(advancedSearchSpecExpr);
                        }
                    }
                } else if (selectValues != null && !selectValues.isEmpty()) {
                    BooleanExpression selectValuesSearchSpec = null;
                    for (final Object currentValue : selectValues) {
                        if (columnDescription != null) {
                            final BooleanExpression columnExpression;
                            final IExpressionSearch<?> iExpressionSearchProvider =
                                    ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());

                            try {
                                columnExpression = iExpressionSearchProvider.createExpression(
                                        columnDescription.getExpression(),
                                        currentValue,
                                        null
                                );
                            } catch (final DataTableUnsupportedExpressionTypeException e) {
                                throw new DataTableQueryException("Invalid search query", e);
                            }

                            if (selectValuesSearchSpec == null) {
                                selectValuesSearchSpec = columnExpression;
                            } else {
                                selectValuesSearchSpec = selectValuesSearchSpec.or(columnExpression);
                            }
                        } else {
                            throw new DataTableQueryException("Invalid search query");
                        }
                    }
                    if (selectValuesSearchSpec != null) {
                        if (result == null) {
                            result = selectValuesSearchSpec;
                        } else {
                            result = result.and(selectValuesSearchSpec);
                        }
                    }
                }
            }
        } else {
            // default query spec
            for (final ColumnUsageDescription<?> columnDescription : columnUageConfiguration.getColumnDescriptions()) {
                if (columnDescription.getDefaultSearchValue() != null) {
                    final BooleanExpression columnExpression;
                    final IExpressionSearch<?> iExpressionSearchProvider =
                            ColumnExpressionTypeFactory.getByExpression(columnDescription.getExpression());

                    try {
                        columnExpression = iExpressionSearchProvider.createExpression(
                                columnDescription.getExpression(),
                                columnDescription.getDefaultSearchValue(),
                                null
                        );
                    } catch (final DataTableUnsupportedExpressionTypeException e) {
                        throw new DataTableQueryException("Invalid search query", e);
                    }

                    if (result == null) {
                        result = columnExpression;
                    } else {
                        result = result.and(columnExpression);
                    }
                }
            }

        }

        // build query by global search term
        if (querySpec != null && querySpec.getGlobalSearch() != null) {
            final String globalSearchString = querySpec.getGlobalSearch();
            if (globalSearchString != null) {
                final String[] splittedGlobalSearchString = globalSearchString.trim().split(" ");
                for (final String currentSplittedGlobalSearchString : splittedGlobalSearchString) {
                    BooleanExpression currentGlobalSearchExpression = null;
                    for (ColumnUsageDescription<?> currentColumnDescription : columnUageConfiguration.getColumnDescriptions()) {
                        final IExpressionSearch<?> iExpressionSearchProvider =
                                ColumnExpressionTypeFactory.getByExpression(currentColumnDescription.getExpression());

                        final BooleanExpression columnExpression;
                        try {
                            columnExpression = iExpressionSearchProvider.createExpression(
                                    currentColumnDescription.getExpression(),
                                    currentSplittedGlobalSearchString,
                                    DataQueryColumnOPDto.LIKE
                            );
                        } catch (final DataTableUnsupportedExpressionTypeException e) {
                            // not relevant for fulltext search
                            continue;
                        }

                        if (currentGlobalSearchExpression == null) {
                            currentGlobalSearchExpression = columnExpression;
                        } else {
                            currentGlobalSearchExpression = currentGlobalSearchExpression.or(columnExpression);
                        }
                    }
                    if (currentGlobalSearchExpression != null) {
                        if (result == null) {
                            result = currentGlobalSearchExpression;
                        } else {
                            result = result.and(currentGlobalSearchExpression);
                        }
                    }
                }
            }
        }

        return result;
    }

}
