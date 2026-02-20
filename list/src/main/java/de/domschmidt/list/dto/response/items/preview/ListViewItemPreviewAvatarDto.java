package de.domschmidt.list.dto.response.items.preview;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("avatar")
@SuperBuilder
@Data
public class ListViewItemPreviewAvatarDto extends AbstractListViewItemPreviewDto {}
