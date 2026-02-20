package de.domschmidt.koku.dto.activity;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuActivityStepDto {
    Long id;
    Boolean deleted;
    Long version;

    String name;

    LocalDateTime updated;
    LocalDateTime recorded;
}
