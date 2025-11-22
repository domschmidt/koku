package de.domschmidt.koku.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
