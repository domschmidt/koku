package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.ICalendarContent;
import de.domschmidt.koku.dto.activity.ActivitySequenceItemDto;
import de.domschmidt.koku.dto.promotion.PromotionDto;
import de.domschmidt.koku.dto.user.KokuUserDetailsDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerAppointmentDto implements ICalendarContent {

    private Long id;

    private LocalDate startDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    private Duration approximatelyDuration;
    private String description;
    private String additionalInfo;
    private BigDecimal approxRevenue;

    private CustomerDto customer;
    private List<CustomerAppointmentActivityDto> activities;
    private List<CustomerAppointmentSoldProductDto> soldProducts;
    private List<ActivitySequenceItemDto> activitySequenceItems;
    private List<PromotionDto> promotions;

    private KokuUserDetailsDto user; // optional. KokuUser.id

}
