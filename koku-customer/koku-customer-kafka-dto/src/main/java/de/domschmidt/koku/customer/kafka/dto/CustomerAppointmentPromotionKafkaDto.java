package de.domschmidt.koku.customer.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAppointmentPromotionKafkaDto {

    Long promotionId;

}
