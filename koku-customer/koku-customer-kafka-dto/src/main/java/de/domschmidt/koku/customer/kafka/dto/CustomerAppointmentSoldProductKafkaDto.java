package de.domschmidt.koku.customer.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAppointmentSoldProductKafkaDto {

    Long productId;
    BigDecimal sellPrice;

}
