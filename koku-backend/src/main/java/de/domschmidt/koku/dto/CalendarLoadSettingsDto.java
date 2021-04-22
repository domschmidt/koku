package de.domschmidt.koku.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalendarLoadSettingsDto {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate start;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end;
    private Boolean loadCustomerAppointments;
    private Boolean loadCustomerBirthdays;
    private Boolean loadPrivateAppointments;

}
