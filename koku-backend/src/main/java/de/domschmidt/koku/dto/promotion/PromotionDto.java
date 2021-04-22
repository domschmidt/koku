package de.domschmidt.koku.dto.promotion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionDto {

    private Long id;
    String name;
    LocalDate startDate;
    LocalDate endDate;
    PromotionProductSettingsDto productSettings;
    PromotionActivitySettingsDto activitySettings;

}
