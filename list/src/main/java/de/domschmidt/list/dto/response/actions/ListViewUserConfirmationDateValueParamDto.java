package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("date-value")
@SuperBuilder
@Data
public class ListViewUserConfirmationDateValueParamDto extends AbstractListViewUserConfirmationParamDto {

    ListViewReference valueReference;

}