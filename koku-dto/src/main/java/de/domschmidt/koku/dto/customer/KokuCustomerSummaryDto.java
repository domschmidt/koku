package de.domschmidt.koku.dto.customer;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuCustomerSummaryDto {

    Long id;

    String fullName;
}
