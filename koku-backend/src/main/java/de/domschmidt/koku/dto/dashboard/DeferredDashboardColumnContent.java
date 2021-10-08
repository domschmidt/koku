package de.domschmidt.koku.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeferredDashboardColumnContent implements IDashboardColumnContent {

    String href;

}
