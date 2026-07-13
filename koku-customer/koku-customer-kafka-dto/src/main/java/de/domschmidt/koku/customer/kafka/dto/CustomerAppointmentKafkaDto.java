package de.domschmidt.koku.customer.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerAppointmentKafkaDto {

    public static final String TOPIC = "customerappointments";
    Long id;
    Boolean deleted;

    LocalDateTime start;
    String description;
    String additionalInfo;
    Long customerId;
    String userId;
    List<CustomerAppointmentActivityKafkaDto> activities;
    List<CustomerAppointmentPromotionKafkaDto> promotions;
    List<CustomerAppointmentSoldProductKafkaDto> soldProducts;

    LocalDateTime updated;
    LocalDateTime recorded;
}
