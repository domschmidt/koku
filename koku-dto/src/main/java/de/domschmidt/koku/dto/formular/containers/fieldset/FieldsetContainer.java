package de.domschmidt.koku.dto.formular.containers.fieldset;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.IFormularContent;
import de.domschmidt.formular.dto.content.containers.AbstractFormContainer;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("fieldset")
@Getter
public class FieldsetContainer extends AbstractFormContainer {

    String title;
    @Builder.Default
    List<IFormularContent> content = new ArrayList<>();

    @Override
    public void addContent(final IFormularContent content) {
        this.content.add(content);
    }

}
