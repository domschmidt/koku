package de.domschmidt.koku.dto.formular.layouts.icon;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("icon")
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IconLayout extends AbstractFormularContent {

    String icon;
}
