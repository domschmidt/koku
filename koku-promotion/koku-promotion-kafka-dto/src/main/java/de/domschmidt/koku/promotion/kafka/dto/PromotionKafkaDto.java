package de.domschmidt.koku.promotion.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

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

    List<Long> productManufacturerIds;

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
