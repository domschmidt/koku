package de.domschmidt.koku.dto.formular.fields.stat;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("stat")
@Getter
public class StatFormularField extends AbstractFormField<String> {

    String title;
    String description;

    @Builder.Default
    String defaultValue = "";

    String icon;
}
