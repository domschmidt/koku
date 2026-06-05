package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("route-based-override")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class ListViewRouteBasedFormularFieldOverrideDto extends ListViewFormularFieldOverrideDto {

    String routeParam;
}
