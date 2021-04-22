package de.domschmidt.koku.service.searchoptions;

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
public class PrivateAppointmentSearchOptions {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate start;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate end;

}
