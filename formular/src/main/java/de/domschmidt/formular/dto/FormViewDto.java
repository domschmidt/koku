package de.domschmidt.formular.dto;

import de.domschmidt.business_logic.api.BusinessRule;
import de.domschmidt.formular.dto.content.IFormularContent;
import java.util.List;
import lombok.Data;

@Data
public class FormViewDto {

    IFormularContent contentRoot;
    List<BusinessRule> businessRules;
    List<AbstractFormViewGlobalEventListenerDto> globalEventListeners;
}
