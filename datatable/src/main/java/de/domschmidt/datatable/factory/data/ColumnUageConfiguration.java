package de.domschmidt.datatable.factory.data;

import com.querydsl.core.types.Expression;
import de.domschmidt.datatable.factory.exception.DataTableColumnIdNotUniqueException;
import lombok.Getter;

import java.util.*;

@Getter
public class ColumnUageConfiguration {

    final Map<String, ColumnUsageDescription<?>> columnDescriptionIndexedById;
    private final List<ColumnUsageDescription<?>> columnDescriptions;

    public ColumnUageConfiguration(
            final ColumnUsageDescription<?>... columnDescriptions
    ) throws DataTableColumnIdNotUniqueException {
        final TreeMap<String, ColumnUsageDescription<?>> newColumnDescriptionIndexedById = new TreeMap<>();
        for (ColumnUsageDescription<?> columnDescription : columnDescriptions) {
            if (newColumnDescriptionIndexedById.containsKey(columnDescription.getId())) {
                throw new DataTableColumnIdNotUniqueException("Repeated column with id \"" + columnDescription.getId() + "\"");
            }
            newColumnDescriptionIndexedById.put(columnDescription.getId(), columnDescription);
        }
        this.columnDescriptionIndexedById = Collections.unmodifiableMap(newColumnDescriptionIndexedById);
        this.columnDescriptions = Collections.unmodifiableList(Arrays.asList(columnDescriptions));
    }

    public Expression<?>[] getSelection() {
        final List<Expression<?>> results = new ArrayList<>();
        for (final ColumnUsageDescription<?> columnDescription : this.columnDescriptions) {
            results.add(columnDescription.getExpression());
        }
        return results.toArray(new Expression<?>[0]);
    }

}
