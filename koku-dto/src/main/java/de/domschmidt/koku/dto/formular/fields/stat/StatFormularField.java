package de.domschmidt.koku.dto.formular.fields.stat;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("stat")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StatFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String title;
    String description;

    @Builder.Default
    String defaultValue = "";

    String icon;
}
