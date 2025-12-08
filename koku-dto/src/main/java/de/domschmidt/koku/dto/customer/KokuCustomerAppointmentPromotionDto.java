package de.domschmidt.koku.dto.customer;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class KokuCustomerAppointmentPromotionDto {

    Long promotionId;

}
