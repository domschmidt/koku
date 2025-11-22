package de.domschmidt.koku.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
