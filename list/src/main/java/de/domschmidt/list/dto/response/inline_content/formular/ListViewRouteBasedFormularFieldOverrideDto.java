package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("route-based-override")
@SuperBuilder
@Data
public class ListViewRouteBasedFormularFieldOverrideDto extends ListViewFormularFieldOverrideDto {

    String routeParam;
}
