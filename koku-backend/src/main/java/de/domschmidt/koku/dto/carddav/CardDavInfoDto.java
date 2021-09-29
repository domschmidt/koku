package de.domschmidt.koku.dto.carddav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDavInfoDto {

    String endpointUrl;
    String user;
    String password;

}
