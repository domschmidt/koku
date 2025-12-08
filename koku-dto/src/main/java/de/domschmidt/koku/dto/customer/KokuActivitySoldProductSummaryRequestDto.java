package de.domschmidt.koku.dto.customer;

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
@FieldNameConstants
public class KokuActivitySoldProductSummaryRequestDto {
    List<KokuCustomerAppointmentSoldProductDto> soldProducts = new ArrayList<>();
    List<KokuCustomerAppointmentPromotionDto> promotions = new ArrayList<>();
    LocalDate date;
    LocalTime time;
}
