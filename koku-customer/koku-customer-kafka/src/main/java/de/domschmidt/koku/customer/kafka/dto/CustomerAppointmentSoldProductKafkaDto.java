package de.domschmidt.koku.customer.kafka.dto;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAppointmentSoldProductKafkaDto {

    Long productId;
    BigDecimal sellPrice;
}
