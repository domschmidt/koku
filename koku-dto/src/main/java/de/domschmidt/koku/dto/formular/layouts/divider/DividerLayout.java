package de.domschmidt.koku.dto.formular.layouts.divider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("divider")
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DividerLayout extends AbstractFormularContent {

    String text;
}
