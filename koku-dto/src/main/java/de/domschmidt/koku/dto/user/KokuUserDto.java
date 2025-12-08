package de.domschmidt.koku.dto.user;


import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

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
