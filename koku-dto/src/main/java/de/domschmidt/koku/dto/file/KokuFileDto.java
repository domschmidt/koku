package de.domschmidt.koku.dto.file;

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
public class KokuFileDto {

    UUID id;
    Boolean deleted;

    String filename;
    String mimeType;
    Long size;
    KokuFileRefDto ref;
    String refId;
    String refName;

    LocalDateTime updated;
    LocalDateTime recorded;

}
