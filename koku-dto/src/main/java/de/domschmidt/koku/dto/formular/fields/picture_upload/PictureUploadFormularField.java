package de.domschmidt.koku.dto.formular.fields.picture_upload;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("picture-upload")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PictureUploadFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;

    @Builder.Default
    String defaultValue = "";
}
