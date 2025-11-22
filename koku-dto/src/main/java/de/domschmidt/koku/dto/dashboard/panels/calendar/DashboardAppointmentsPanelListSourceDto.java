package de.domschmidt.koku.dto.dashboard.panels.calendar;

import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.listquery.dto.request.EnumSearchOperatorHint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DashboardAppointmentsPanelListSourceDto {

    String sourceUrl;
    String idPath;
    String startDateFieldSelectionPath;
    String endDateFieldSelectionPath;
    String startTimeFieldSelectionPath;
    String endTimeFieldSelectionPath;
    EnumSearchOperatorHint searchOperatorHint;
    String textFieldSelectionPath;
    String notesTextFieldSelectionPath;
    String sourceItemText;
    KokuColorEnum sourceItemColor;
    Boolean allDay;
    String userIdFieldSelectionPath;
    String deletedFieldSelectionPath;

}
