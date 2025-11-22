package de.domschmidt.list.dto.response.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("date-value")
@SuperBuilder
@Data
public class ListViewNotificationEventDateValueParamDto extends AbstractListViewNotificationEventParamDto {

    ListViewReference valueReference;

}
