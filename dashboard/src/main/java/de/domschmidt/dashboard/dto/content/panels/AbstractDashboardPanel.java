package de.domschmidt.dashboard.dto.content.panels;

import de.domschmidt.dashboard.dto.content.IDashboardContent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Data
public abstract class AbstractDashboardPanel implements IDashboardContent {

    String id;

}
