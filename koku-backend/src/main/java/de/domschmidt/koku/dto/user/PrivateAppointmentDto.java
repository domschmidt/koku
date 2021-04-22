package de.domschmidt.koku.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.ICalendarContent;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivateAppointmentDto implements ICalendarContent {

    private Long id;

    private LocalDate startDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    private LocalDate endDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private String description;

}
