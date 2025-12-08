package de.domschmidt.koku.dto.user;


import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@FieldNameConstants
public class KokuUserSummaryDto {

    String id;

    String summary;

}
