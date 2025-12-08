package de.domschmidt.koku.dto.user;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class KokuUserAppointmentDto {

    Long id;
    Boolean deleted;
    Long version;

    String userId;
    String userName;
    String summary;
    LocalDate startDate;
    LocalTime startTime;
    LocalDate endDate;
    LocalTime endTime;
    String description;

    LocalDateTime updated;
    LocalDateTime recorded;

}
