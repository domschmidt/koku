package de.domschmidt.koku.dto.formular.containers.grid;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.IFormularContent;
import de.domschmidt.formular.dto.content.containers.AbstractFormContainer;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("grid")
@Getter
public class GridContainer extends AbstractFormContainer {

    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;
    Integer xl2;
    Integer cols;

    @Builder.Default
    List<IFormularContent> content = new ArrayList<>();

    @Override
    public void addContent(final IFormularContent content) {
        this.content.add(content);
    }
}
