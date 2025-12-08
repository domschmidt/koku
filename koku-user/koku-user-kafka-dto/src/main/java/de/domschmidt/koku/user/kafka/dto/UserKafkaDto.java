package de.domschmidt.koku.user.kafka.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder

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
