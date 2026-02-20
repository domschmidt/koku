package de.domschmidt.koku.dto.customer;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuCustomerAppointmentSummaryDto {
    Long id;

    String appointmentSummary;
}
