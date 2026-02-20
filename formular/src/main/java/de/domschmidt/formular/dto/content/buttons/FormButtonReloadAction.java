package de.domschmidt.formular.dto.content.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@JsonTypeName("reload")
@SuperBuilder
public class FormButtonReloadAction extends AbstractFormButtonButtonAction {}
