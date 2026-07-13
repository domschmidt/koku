package de.domschmidt.formular.dto;

import de.domschmidt.formular.dto.content.AbstractFormularContent;
import de.domschmidt.koku.business_logic.dto.KokuBusinessRuleDto;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class FormViewDto {

    String alias;
    String rootId;
    Map<String, AbstractFormularContent> contents;
    List<FormPlacementDto> placements;
    List<KokuBusinessRuleDto> businessRules;
    List<AbstractFormViewGlobalEventListenerDto> globalEventListeners;
}
