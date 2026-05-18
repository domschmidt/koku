package de.domschmidt.koku.dto.formular.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("notification")
@SuperBuilder
@Data
public class FormNotificationEvent extends AbstractFormEventDto {

    FormNotificationEventSerenityEnumDto serenity;
    String text;
    List<AbstractFormNotificationEventParamDto> params = new ArrayList<>();
}
