package de.domschmidt.koku.customer.kafka.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAppointmentPromotionKafkaDto {

    Long promotionId;
}
