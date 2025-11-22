package de.domschmidt.koku.dto.activity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuActivityDto {
    Long id;
    Boolean deleted;
    Long version;

    String name;
    LocalTime approximatelyDuration;
    BigDecimal price;

    LocalDateTime updated;
    LocalDateTime recorded;
}
