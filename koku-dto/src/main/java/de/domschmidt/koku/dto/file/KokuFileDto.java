package de.domschmidt.koku.dto.file;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuFileDto {

    UUID id;
    Boolean deleted;

    String filename;
    String mimeType;
    Long size;
    Long customerId;

    LocalDateTime updated;
    LocalDateTime recorded;

}
