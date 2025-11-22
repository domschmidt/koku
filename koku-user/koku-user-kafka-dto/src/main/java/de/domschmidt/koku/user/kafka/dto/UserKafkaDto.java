package de.domschmidt.koku.user.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class UserKafkaDto {

    public static final String TOPIC = "users";
    String id;

    Boolean deleted;

    String firstname;
    String lastname;
    String avatarBase64;
    String countryIso;
    String stateIso;

    LocalDateTime updated;
    LocalDateTime recorded;

}
