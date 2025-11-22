package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@JsonTypeName("formular")
public class KokuBusinessRuleFormularContentDto extends AbstractKokuBusinessRuleContentDto {

    String formularUrl;
    String sourceUrl;
    String submitUrl;
    KokuBusinessRuleFormularActionSubmitMethodEnumDto submitMethod;
    Integer maxWidthInPx;
    List<AbstractKokuBusinessRuleFormularContentSaveEventDto> onSaveEvents;
    List<KokuBusinessRuleFormularFieldOverrideDto> fieldOverrides;

}
