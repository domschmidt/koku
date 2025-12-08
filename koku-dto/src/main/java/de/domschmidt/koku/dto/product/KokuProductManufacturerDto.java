package de.domschmidt.koku.dto.product;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuProductManufacturerDto {

    Long id;
    Boolean deleted;
    Long version;

    String name;

    LocalDateTime updated;
    LocalDateTime recorded;

}
