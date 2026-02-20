package de.domschmidt.koku.dto.dashboard.containers.grid;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.dashboard.dto.content.IDashboardContent;
import de.domschmidt.dashboard.dto.content.containers.AbstractDashboardContainer;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("grid")
@Getter
public class DashboardGridContainerDto extends AbstractDashboardContainer {

    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;
    Integer xl2;
    Integer xl3;
    Integer xl4;
    Integer xl5;
    Integer cols;
    Integer maxWidthInPx;

    @Builder.Default
    List<IDashboardContent> content = new ArrayList<>();

    @Override
    public void addContent(final IDashboardContent content) {
        this.content.add(content);
    }
}
