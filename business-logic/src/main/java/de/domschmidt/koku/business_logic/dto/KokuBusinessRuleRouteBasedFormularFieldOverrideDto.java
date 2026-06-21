package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("route-based-override")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class KokuBusinessRuleRouteBasedFormularFieldOverrideDto extends KokuBusinessRuleFormularContentOverrideDto {

    String routeParam;
}
