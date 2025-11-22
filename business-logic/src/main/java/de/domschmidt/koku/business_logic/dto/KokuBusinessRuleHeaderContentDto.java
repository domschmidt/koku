package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@JsonTypeName("header")
public class KokuBusinessRuleHeaderContentDto extends AbstractKokuBusinessRuleContentDto {

    String title;

    String sourceUrl;
    String titlePath;

    AbstractKokuBusinessRuleContentDto content;

    @Builder.Default
    List<AbstractKokuBusinessRuleHeaderContentGlobalEventListenersDto> globalEventListeners = new ArrayList<>();

}
