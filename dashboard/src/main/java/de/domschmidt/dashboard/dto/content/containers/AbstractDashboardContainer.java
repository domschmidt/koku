package de.domschmidt.dashboard.dto.content.containers;

import de.domschmidt.dashboard.dto.content.IDashboardContent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Data
public abstract class AbstractDashboardContainer implements IDashboardContent {

    String id;

    public abstract void addContent(IDashboardContent content);
}
