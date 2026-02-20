package de.domschmidt.list.dto.response.items.preview;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("text")
@SuperBuilder
@Data
public class ListViewItemPreviewTextDto extends AbstractListViewItemPreviewDto {}
