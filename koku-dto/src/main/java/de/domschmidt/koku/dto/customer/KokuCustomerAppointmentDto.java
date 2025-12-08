package de.domschmidt.koku.dto.customer;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder

@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuCustomerAppointmentDto {

    Long id;
    Boolean deleted;
    Long version;

    Long customerId;
    String customerName;
    String shortSummaryText;
    String longSummaryText;
    LocalDate date;
    LocalTime time;
    LocalDate approximatelyEndDate;
    LocalTime approximatelyEndTime;
    List<KokuCustomerAppointmentActivityDto> activities;
    List<KokuCustomerAppointmentTreatmentDto> treatmentSequence;
    List<KokuCustomerAppointmentSoldProductDto> soldProducts;
    List<KokuCustomerAppointmentPromotionDto> promotions;
    String activityPriceSummary;
    String activityDurationSummary;
    String activitySoldProductSummary;
    String description;
    String additionalInfo;
    String userId;
    LocalDateTime updated;
    LocalDateTime recorded;

}
