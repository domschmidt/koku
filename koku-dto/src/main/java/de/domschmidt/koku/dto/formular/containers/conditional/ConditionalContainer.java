package de.domschmidt.koku.dto.formular.containers.conditional;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("condition")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConditionalContainer extends AbstractFormularContent {

    String compareValuePath;

    @Singular
    List<Object> expectedValues;
}
