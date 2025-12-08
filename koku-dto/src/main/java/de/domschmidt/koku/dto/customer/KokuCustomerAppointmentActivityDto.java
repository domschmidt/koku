package de.domschmidt.koku.dto.customer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class KokuCustomerAppointmentActivityDto {

    Long activityId;
    @DecimalMin(value = "0.0")
    @Digits(integer = 3, fraction = 2)
    BigDecimal price;

}
