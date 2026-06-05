package de.domschmidt.koku.dto.formular.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.koku.dto.formular.user_confirmation.AbstractFormUserConfirmationParamDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("source-path")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class FormButtonUserConfirmationSourcePathParamDto extends AbstractFormUserConfirmationParamDto {

    String sourcePath;
}
