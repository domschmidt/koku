package de.domschmidt.koku.dto.formular.user_confirmation;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FormUserConfirmationDto {

    String headline;
    String content;

    @Builder.Default
    List<AbstractFormUserConfirmationParamDto> params = new ArrayList<>();
}
