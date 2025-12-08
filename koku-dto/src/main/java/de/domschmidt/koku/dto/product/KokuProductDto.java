package de.domschmidt.koku.dto.product;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuProductDto {

    Long id;
    Boolean deleted;
    Long version;

    String name;
    Long manufacturerId;
    String manufacturerName;
    BigDecimal price;
    String formattedPrice;

    LocalDateTime updated;
    LocalDateTime recorded;
}
