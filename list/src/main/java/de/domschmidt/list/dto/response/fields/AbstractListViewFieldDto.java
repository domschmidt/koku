package de.domschmidt.list.dto.response.fields;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public abstract class AbstractListViewFieldDto<T> implements IListViewFieldDefault<T> {

    String id;
}
