package de.domschmidt.koku.dto.panels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class TextPanelDto extends PanelDto {

    List<TextPanelContent> texts;

}
