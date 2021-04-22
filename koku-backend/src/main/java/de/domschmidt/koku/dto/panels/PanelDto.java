package de.domschmidt.koku.dto.panels;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode
public abstract class PanelDto {
    String title;
}
