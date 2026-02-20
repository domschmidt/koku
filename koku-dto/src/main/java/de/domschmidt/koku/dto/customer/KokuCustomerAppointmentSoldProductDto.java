package de.domschmidt.koku.dto.customer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
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
public class KokuCustomerAppointmentSoldProductDto {

    Long productId;

    @DecimalMin(value = "0.0")
    @Digits(integer = 3, fraction = 2)
    BigDecimal price;
}
