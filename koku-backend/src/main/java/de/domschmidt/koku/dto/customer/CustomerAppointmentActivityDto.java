package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.activity.ActivityDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerAppointmentActivityDto {

    Long id;
    ActivityDto activity;
    BigDecimal sellPrice;

}
