package de.domschmidt.formular.dto.content;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class AbstractFormularContent {

    private String id;
    private String alias;
}
