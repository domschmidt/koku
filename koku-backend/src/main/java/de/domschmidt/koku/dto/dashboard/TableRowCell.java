package de.domschmidt.koku.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(value = StringTableRowCell.class, name = "StringTableRowCell"),
                @JsonSubTypes.Type(value = NumberTableRowCell.class, name = "NumberTableRowCell"),
        }
)
public abstract class TableRowCell<T> {

    T value;

}
