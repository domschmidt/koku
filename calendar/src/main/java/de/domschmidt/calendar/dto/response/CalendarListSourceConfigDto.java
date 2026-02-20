package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.listquery.dto.request.EnumSearchOperatorHint;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldNameConstants
@JsonTypeName("list")
@SuperBuilder
@Data
public class CalendarListSourceConfigDto extends AbstractCalendarListSourceConfigDto {

    String sourceUrl;

    String sourceItemText;
    Boolean editable;
    String idPath;
    String startDateFieldSelectionPath;
    String endDateFieldSelectionPath;
    String startTimeFieldSelectionPath;
    String endTimeFieldSelectionPath;
    String userIdFieldSelectionPath;
    EnumSearchOperatorHint searchOperatorHint;
    List<String> additionalFieldSelectionPaths;
    String displayTextFieldSelectionPath;
    String deletedFieldSelectionPath;
}
