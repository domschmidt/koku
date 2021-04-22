package de.domschmidt.koku.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.customer.CustomerAppointmentDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentGroupDto {

    private LocalDate date;
    private List<CustomerAppointmentDto> appointments;

}
