package de.domschmidt.koku.dto.formular.user_confirmation;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class FormUserConfirmationDto {

    String headline;
    String content;
    List<AbstractFormUserConfirmationParamDto> params = new ArrayList<>();
}
