package de.domschmidt.koku.dto.user;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class KokuUserDto {

    String id;
    Boolean deleted;
    Long version;

    String firstname;
    String fullname;
    String initials;
    String lastname;
    String avatarBase64;
    Long regionId;

    LocalDateTime updated;
    LocalDateTime recorded;
}
