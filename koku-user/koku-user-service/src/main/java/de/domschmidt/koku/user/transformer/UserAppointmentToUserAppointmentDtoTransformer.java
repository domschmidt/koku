package de.domschmidt.koku.user.transformer;

import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserAppointment;
import jakarta.persistence.EntityManager;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAppointmentToUserAppointmentDtoTransformer {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final EntityManager entityManager;

    @Autowired
    public UserAppointmentToUserAppointmentDtoTransformer(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public KokuUserAppointmentDto transformToDto(final UserAppointment model) {
        return KokuUserAppointmentDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .startDate(
                        model.getStartTimestamp() != null
                                ? model.getStartTimestamp().toLocalDate()
                                : null)
                .startTime(
                        model.getStartTimestamp() != null
                                ? model.getStartTimestamp().toLocalTime()
                                : null)
                .endDate(
                        model.getEndTimestamp() != null
                                ? model.getEndTimestamp().toLocalDate()
                                : null)
                .endTime(
                        model.getEndTimestamp() != null
                                ? model.getEndTimestamp().toLocalTime()
                                : null)
                .description(model.getDescription())
                .userId(model.getUser().getId())
                .userName(Stream.of(
                                model.getUser().getFirstname(), model.getUser().getLastname())
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(" ")))
                .summary(String.format("Privater Termin vom %s", DATE_FORMATTER.format(model.getStartTimestamp())))
                .build();
    }

    public UserAppointment transformToEntity(final UserAppointment model, final KokuUserAppointmentDto updatedDto) {
        if (updatedDto.getStartDate() != null && updatedDto.getStartTime() != null) {
            model.setStartTimestamp(updatedDto.getStartDate().atTime(updatedDto.getStartTime()));
        }
        if (updatedDto.getEndDate() != null && updatedDto.getEndTime() != null) {
            model.setEndTimestamp(updatedDto.getEndDate().atTime(updatedDto.getEndTime()));
        }
        if (updatedDto.getDescription() != null) {
            model.setDescription(updatedDto.getDescription());
        }
        if (updatedDto.getUserId() != null) {
            model.setUser(this.entityManager.getReference(User.class, updatedDto.getUserId()));
        }
        return model;
    }
}
