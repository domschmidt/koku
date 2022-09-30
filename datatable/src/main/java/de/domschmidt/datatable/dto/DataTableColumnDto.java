package de.domschmidt.datatable.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.domschmidt.datatable.dto.query.DataQueryColumnSortDirDto;
import lombok.Getter;

import java.util.Collection;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTableColumnDto<T, S> {
    @JsonProperty(required = true)
    final String id;
    @JsonProperty(required = true)
    final String name;
    @JsonProperty(required = true)
    final String type;
    final Boolean isKey;
    final Boolean canSort;
    final Boolean canFilter;
    final DataQueryColumnSortDirDto defaultSortDir;
    final Integer defaultSortIdx;
    final Boolean hidden;
    final T footerSummary;
    final T defaultSearchValue;
    final S typeSpecificSettings;
    final Collection<T> possibleSelectValues;

    public DataTableColumnDto(
            final String id,
            final String name,
            final String type,
            final Boolean isKey,
            final Boolean canSort,
            final Boolean canFilter,
            final DataQueryColumnSortDirDto defaultSortDir,
            final Integer defaultSortIdx,
            final Boolean hidden,
            final T footerSummary,
            final T defaultSearchValue,
            final S typeSpecificSettings,
            final Collection<T> possibleSelectValues
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isKey = isKey;
        this.canSort = canSort;
        this.canFilter = canFilter;
        this.defaultSortDir = defaultSortDir;
        this.defaultSortIdx = defaultSortIdx;
        this.hidden = hidden;
        this.footerSummary = footerSummary;
        this.defaultSearchValue = defaultSearchValue;
        this.typeSpecificSettings = typeSpecificSettings;
        this.possibleSelectValues = possibleSelectValues;
    }

}
