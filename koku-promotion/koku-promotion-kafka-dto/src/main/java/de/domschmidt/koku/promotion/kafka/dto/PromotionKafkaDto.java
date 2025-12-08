package de.domschmidt.koku.promotion.kafka.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder

@AllArgsConstructor
@NoArgsConstructor
public class PromotionKafkaDto {

    public static final String TOPIC = "promotions";
    Long id;

    Boolean deleted;
    String name;

    LocalDate startDate;
    LocalDate endDate;

    BigDecimal activityAbsoluteItemSavings;
    BigDecimal activityAbsoluteSavings;
    BigDecimal activityRelativeItemSavings;
    BigDecimal activityRelativeSavings;
    BigDecimal productAbsoluteItemSavings;
    BigDecimal productAbsoluteSavings;
    BigDecimal productRelativeItemSavings;
    BigDecimal productRelativeSavings;

    LocalDateTime updated;
    LocalDateTime recorded;

}
