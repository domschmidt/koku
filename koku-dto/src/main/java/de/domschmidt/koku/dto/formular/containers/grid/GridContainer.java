package de.domschmidt.koku.dto.formular.containers.grid;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("grid")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GridContainer extends AbstractFormularContent {

    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;
    Integer xl2;
    Integer cols;
}
