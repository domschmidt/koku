package de.domschmidt.koku.dto.list.fields.picture_upload;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("picture-upload")
@Getter
public class ListViewPictureUploadFieldDto extends AbstractListViewFieldDto<String> {

    String label;

    @Builder.Default
    String defaultValue = "";
}
