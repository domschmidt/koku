package de.domschmidt.koku.dto.formular.buttons;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import de.domschmidt.formular.dto.content.buttons.AbstractFormButtonButtonAction;
import de.domschmidt.formular.dto.content.buttons.EnumButtonType;
import de.domschmidt.koku.dto.formular.events.AbstractFormEventDto;
import de.domschmidt.koku.dto.formular.events.FormNotificationEvent;
import de.domschmidt.koku.dto.formular.events.FormNotificationEventSerenityEnumDto;
import de.domschmidt.koku.dto.formular.user_confirmation.FormUserConfirmationDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("button")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KokuFormButton extends AbstractFormularContent {

    Boolean disabled;
    EnumButtonType buttonType;
    String href;
    EnumLinkTarget hrefTarget;
    String title;
    String text;
    String icon;
    Boolean loading;
    Boolean dockable;
    ButtonDockableSettings dockableSettings;
    List<EnumButtonStyle> styles;
    Object submitPayload;

    @Singular
    List<AbstractFormButtonButtonAction> postProcessingActions;

    FormUserConfirmationDto userConfirmation;

    @Builder.Default
    List<AbstractFormEventDto> successEvents = List.of(FormNotificationEvent.builder()
            .text("Erfolgreich gespeichert")
            .serenity(FormNotificationEventSerenityEnumDto.SUCCESS)
            .build());

    @Builder.Default
    List<AbstractFormEventDto> failEvents = List.of(FormNotificationEvent.builder()
            .text("Fehler beim Speichern")
            .serenity(FormNotificationEventSerenityEnumDto.ERROR)
            .build());
}
