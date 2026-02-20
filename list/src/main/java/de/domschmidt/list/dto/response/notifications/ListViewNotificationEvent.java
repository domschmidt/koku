package de.domschmidt.list.dto.response.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewActionEventDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("notification")
@SuperBuilder
@Data
public class ListViewNotificationEvent extends AbstractListViewActionEventDto {

    ListViewNotificationEventSerenityEnumDto serenity;
    String text;
    List<AbstractListViewNotificationEventParamDto> params = new ArrayList<>();
}
