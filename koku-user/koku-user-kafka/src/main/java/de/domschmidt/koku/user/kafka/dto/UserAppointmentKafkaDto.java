package de.domschmidt.koku.user.kafka.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAppointmentKafkaDto {

    public static final String TOPIC = "userappointments";

    Long id;
    Boolean deleted;
    LocalDateTime start;
    LocalDateTime end;
    String description;
    String userId;
    LocalDateTime updated;
    LocalDateTime recorded;
}
