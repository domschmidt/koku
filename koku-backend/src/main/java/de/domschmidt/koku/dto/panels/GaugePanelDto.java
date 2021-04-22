package de.domschmidt.koku.dto.panels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class GaugePanelDto extends PanelDto {

    Integer percentage; // 0-100

}
