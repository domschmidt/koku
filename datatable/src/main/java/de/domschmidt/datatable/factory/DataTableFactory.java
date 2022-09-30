package de.domschmidt.datatable.factory;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.DataTableColumnDto;
import de.domschmidt.datatable.dto.DataTableDto;
import de.domschmidt.datatable.factory.data.ColumnUageConfiguration;
import de.domschmidt.datatable.factory.data.ColumnUsageDescription;
import de.domschmidt.datatable.factory.data.IEnumStringValue;
import de.domschmidt.datatable.factory.dto_type_specifics.DtoTypeSpecificSettingsTransformerFactory;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;
import de.domschmidt.datatable.factory.exception.DataTableException;
import org.apache.commons.beanutils.PropertyUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataTableFactory<T> {

    private final QueryResults<T> listOfEntities;
    private final ColumnUageConfiguration columnUageConfiguration;
    private final String tableName;

    public DataTableFactory(
            final QueryResults<T> listOfEntities,
            final ColumnUageConfiguration columnUageConfiguration,
            final String tableName
    ) {
        this.listOfEntities = listOfEntities;
        this.columnUageConfiguration = columnUageConfiguration;
        this.tableName = tableName;
    }

    public DataTableDto buildTable() throws DataTableException {
        return DataTableDto.builder()
                .columns(buildColumns())
                .rows(buildRows())
                .tableName(this.tableName)
                .pageSize(this.listOfEntities.getResults().size())
                .page(((int) Math.floor(this.listOfEntities.getOffset() * 1d / this.listOfEntities.getResults().size()) + 1) - 1)
                .total(this.listOfEntities.getTotal())
                .totalPages(this.listOfEntities.getResults().size() < 1 ? 0 : (int) Math.ceil(this.listOfEntities.getTotal() * 1d / this.listOfEntities.getResults().size()))
                .build();
    }

    private List<Map<String, Object>> buildRows() throws DataTableException {
        final List<Map<String, Object>> result = new ArrayList<>();
        try {
            for (final T listOfEntity : this.listOfEntities.getResults()) {
                final Map<String, Object> currentRowCells = new TreeMap<>();
                for (final ColumnUsageDescription<?> columnDescription : this.columnUageConfiguration.getColumnDescriptions()) {
                    final String currentColumnName = columnDescription.getId();
                    final Object currentColumnValue;
                    if (listOfEntity instanceof Tuple) {
                        // multiple entities are involved.
                        Object entity = ((Tuple) listOfEntity).get(columnDescription.getExpression());
                        if (entity == null) {
                            if (columnDescription.getExpression() instanceof Path) {
                                entity = ((Tuple) listOfEntity).get(((Path<?>) columnDescription.getExpression()).getRoot());
                                if (entity != null) {
                                    currentColumnValue = PropertyUtils.getNestedProperty(entity, currentColumnName);
                                } else {
                                    currentColumnValue = null;
                                }
                            } else {
                                currentColumnValue = null;
                            }
                        } else {
                            currentColumnValue = entity;
                        }
                    } else {
                        // only one concrete entity is involved.
                        currentColumnValue = PropertyUtils.getNestedProperty(listOfEntity, currentColumnName);
                    }
                    if (currentColumnValue != null && currentColumnValue.getClass().isEnum()) {
                        if (currentColumnValue instanceof IEnumStringValue) {
                            currentRowCells.put(columnDescription.getId(), ((IEnumStringValue<?>) currentColumnValue).getUserPresentableValue());
                        } else {
                            currentRowCells.put(columnDescription.getId(), currentColumnValue);
                        }
                    } else {
                        currentRowCells.put(columnDescription.getId(), currentColumnValue);
                    }
                }
                result.add(currentRowCells);
            }
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new DataTableException(e);
        }
        return result;
    }

    private List<DataTableColumnDto<?, ?>> buildColumns() {
        final List<DataTableColumnDto<?, ?>> result = new ArrayList<>();
        for (final ColumnUsageDescription<?> columnDescription : this.columnUageConfiguration.getColumnDescriptions()) {
            result.add(buildColumnDto(
                    columnDescription
            ));
        }
        return result;
    }

    private <T> DataTableColumnDto<?, ?> buildColumnDto(
            final ColumnUsageDescription<T> columnDescription
    ) {
        final Boolean isKey;
        final Boolean canSort;
        final Boolean canFilter;
        final ITypeSpecificSettingsTransformer<?> typeSpecificSettingsTransformer = DtoTypeSpecificSettingsTransformerFactory.getByType(
                columnDescription.getExpression().getType()
        );
        final Object typeSpecificSettings;
        final String dtoType;
        if (columnDescription.getExpression() instanceof final Path<?> dslQPath) {
            if (dslQPath.getAnnotatedElement().getDeclaredAnnotation(Id.class) != null) {
                isKey = true;
            } else {
                final Column columnAnnotation = dslQPath.getAnnotatedElement().getDeclaredAnnotation(Column.class);
                if (columnAnnotation != null) {
                    isKey = columnAnnotation.unique();
                } else {
                    isKey = null;
                }
            }
            canSort = true;
            canFilter = true;
            if (typeSpecificSettingsTransformer != null) {
                typeSpecificSettings = typeSpecificSettingsTransformer.transformTypeSpecificSettingsByPath(dslQPath);
                dtoType = typeSpecificSettingsTransformer.getDtoType(dslQPath.getType());
            } else {
                typeSpecificSettings = null;
                dtoType = dslQPath.getType().getSimpleName();
            }
        } else if (columnDescription.getExpression() instanceof final Operation<?> dslQPath) {
            isKey = null;
            canSort = true;
            canFilter = true;
            if (typeSpecificSettingsTransformer != null) {
                typeSpecificSettings = null;
                dtoType = typeSpecificSettingsTransformer.getDtoType(dslQPath.getType());
            } else {
                typeSpecificSettings = null;
                dtoType = dslQPath.getType().getSimpleName();
            }
        } else {
            isKey = null;
            canSort = null;
            canFilter = null;
            typeSpecificSettings = null;
            dtoType = columnDescription.getExpression().getType().getSimpleName();
        }
        return new DataTableColumnDto<>(
                columnDescription.getId(),
                columnDescription.getColumnName(),
                columnDescription.getCustomDtoType() != null ? columnDescription.getCustomDtoType() : dtoType,
                isKey,
                canSort,
                canFilter,
                columnDescription.getDefaultSortDir(),
                columnDescription.getDefaultSortIdx(),
                columnDescription.getHidden(),
                columnDescription.getSummary(),
                columnDescription.getDefaultSearchValue(),
                typeSpecificSettings,
                columnDescription.getPossibleSelectValues() != null ? columnDescription.getPossibleSelectValues() : null
        );
    }
}
