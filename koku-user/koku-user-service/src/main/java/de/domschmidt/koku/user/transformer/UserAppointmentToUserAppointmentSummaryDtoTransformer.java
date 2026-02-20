package de.domschmidt.koku.user.transformer;

import de.domschmidt.koku.dto.user.KokuUserAppointmentSummaryDto;
import de.domschmidt.koku.user.persistence.UserAppointment;
import java.time.format.DateTimeFormatter;

public class UserAppointmentToUserAppointmentSummaryDtoTransformer {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public KokuUserAppointmentSummaryDto transformToSummaryDto(final UserAppointment model) {
        return KokuUserAppointmentSummaryDto.builder()
                .id(model.getId())
                .summary(String.format("Privater Termin vom %s", DATE_FORMATTER.format(model.getStartTimestamp())))
                .build();
    }
}
