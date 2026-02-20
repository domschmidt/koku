package de.domschmidt.formular.dto;

import de.domschmidt.formular.dto.content.IFormularContent;
import de.domschmidt.koku.business_logic.dto.KokuBusinessRuleDto;
import java.util.List;
import lombok.Data;

@Data
public class FormViewDto {

    IFormularContent contentRoot;
    List<KokuBusinessRuleDto> businessRules;
    List<AbstractFormViewGlobalEventListenerDto> globalEventListeners;
}
