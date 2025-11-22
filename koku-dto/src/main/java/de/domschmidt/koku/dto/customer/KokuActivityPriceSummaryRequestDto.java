package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldNameConstants
public class KokuActivityPriceSummaryRequestDto {

    List<KokuCustomerAppointmentActivityDto> activities = new ArrayList<>();
    List<KokuCustomerAppointmentPromotionDto> promotions = new ArrayList<>();
    LocalDate date;
    LocalTime time;

}
