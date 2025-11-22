package de.domschmidt.koku.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
