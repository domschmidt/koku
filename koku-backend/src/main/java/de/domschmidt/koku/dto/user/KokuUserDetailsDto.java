package de.domschmidt.koku.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KokuUserDetailsDto {

    private Long id;

    private String username;
    private String firstname;
    private String lastname;
    private String avatarBase64;
    private String password;

}
