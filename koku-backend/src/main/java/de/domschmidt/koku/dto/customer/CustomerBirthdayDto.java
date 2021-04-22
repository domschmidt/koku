package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.ICalendarContent;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerBirthdayDto implements ICalendarContent {

    private Long id;
    private LocalDate birthday;
    private String firstName;
    private String lastName;

}
