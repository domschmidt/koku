package de.domschmidt.koku.dto.formular.containers.conditional;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.IFormularContent;
import de.domschmidt.formular.dto.content.containers.AbstractFormContainer;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("condition")
@Getter
public class ConditionalContainer extends AbstractFormContainer {

    String compareValuePath;

    @Singular
    List<Object> expectedValues = new ArrayList<>();

    @Builder.Default
    List<IFormularContent> content = new ArrayList<>();

    @Override
    public void addContent(final IFormularContent content) {
        this.content.add(content);
    }
}
