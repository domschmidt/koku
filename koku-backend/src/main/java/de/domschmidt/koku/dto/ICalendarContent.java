package de.domschmidt.koku.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.domschmidt.koku.dto.customer.CustomerAppointmentDto;
import de.domschmidt.koku.dto.customer.CustomerBirthdayDto;
import de.domschmidt.koku.dto.user.PrivateAppointmentDto;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(value = CustomerAppointmentDto.class, name = "CustomerAppointment"),
                @JsonSubTypes.Type(value = CustomerBirthdayDto.class, name = "CustomerBirthday"),
                @JsonSubTypes.Type(value = PrivateAppointmentDto.class, name = "PrivateAppointment"),
        }
)
public interface ICalendarContent {
}
