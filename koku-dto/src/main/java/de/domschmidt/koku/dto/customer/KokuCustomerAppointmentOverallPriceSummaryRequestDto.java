package de.domschmidt.koku.dto.customer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class KokuCustomerAppointmentOverallPriceSummaryRequestDto {

    List<KokuCustomerAppointmentActivityDto> activities = new ArrayList<>();
    List<KokuCustomerAppointmentSoldProductDto> soldProducts = new ArrayList<>();
    List<KokuCustomerAppointmentPromotionDto> promotions = new ArrayList<>();
    LocalDate date;
    LocalTime time;
}
