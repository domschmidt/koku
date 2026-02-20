package de.domschmidt.koku.dto.document;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuDocumentDto {

    Long id;
    String name;
    Boolean deleted;
    Long version;

    String template;
    UUID ref;

    LocalDateTime updated;
    LocalDateTime recorded;
}
