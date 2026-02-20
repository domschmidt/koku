package de.domschmidt.koku.dto.formular.fields.documents;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("document-designer")
@Getter
public class DocumentDesignerFormularField extends AbstractFormField<String> {

    @Builder.Default
    String defaultValue = "";
}
