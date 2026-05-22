package de.domschmidt.koku.dto.formular.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("notification")
@SuperBuilder
@Data
public class FormNotificationEvent extends AbstractFormEventDto {

    FormNotificationEventSerenityEnumDto serenity;
    String text;

    @Builder.Default
    List<AbstractFormNotificationEventParamDto> params = new ArrayList<>();
}
