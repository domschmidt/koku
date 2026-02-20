package de.domschmidt.koku.dto.formular.fields.picture_upload;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("picture-upload")
@Getter
public class PictureUploadFormularField extends AbstractFormField<String> {

    String label;

    @Builder.Default
    String defaultValue = "";
}
