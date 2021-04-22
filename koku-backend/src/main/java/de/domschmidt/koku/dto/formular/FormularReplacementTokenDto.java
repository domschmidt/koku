package de.domschmidt.koku.dto.formular;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormularReplacementTokenDto {

    String tokenName;
    String replacementToken;

}
