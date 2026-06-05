package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonTypeName("open-dialog-content")
public class KokuBusinessRuleOpenDialogContentDto extends AbstractKokuBusinessRuleExecutionDto {

    AbstractKokuBusinessRuleContentDto content;

    @Singular
    List<AbstractKokuBusinessRuleOpenContentCloseListenerDto> closeEventListeners;
}
