package de.domschmidt.koku.customer.kafka.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAppointmentSoldProductKafkaDto {

    Long productId;
    BigDecimal sellPrice;

}
