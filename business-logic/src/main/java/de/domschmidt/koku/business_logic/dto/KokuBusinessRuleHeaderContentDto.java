package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
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
