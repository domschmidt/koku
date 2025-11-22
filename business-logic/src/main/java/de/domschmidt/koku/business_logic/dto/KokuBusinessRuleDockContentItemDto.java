package de.domschmidt.koku.business_logic.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KokuBusinessRuleDockContentItemDto {

    String id;
    String title;
    String route;
    String icon;
    AbstractKokuBusinessRuleContentDto content;

}
