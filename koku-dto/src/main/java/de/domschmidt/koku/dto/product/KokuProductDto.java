package de.domschmidt.koku.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
