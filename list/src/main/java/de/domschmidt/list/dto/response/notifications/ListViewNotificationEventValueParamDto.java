package de.domschmidt.list.dto.response.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("value")
@SuperBuilder
@Data
public class ListViewNotificationEventValueParamDto extends AbstractListViewNotificationEventParamDto {

    ListViewReference valueReference;

}
