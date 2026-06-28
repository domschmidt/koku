package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.dav.kafka.users.service.UserAppointmentKTableProcessor;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Repository;

@Repository
public class UserAppointmentRepository {

    private final UserAppointmentKTableProcessor userAppointmentKTableProcessor;

    public UserAppointmentRepository(final UserAppointmentKTableProcessor userAppointmentKTableProcessor) {
        this.userAppointmentKTableProcessor = userAppointmentKTableProcessor;
    }

    public List<UserAppointmentKafkaDto> findAllAppointments() {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                userAppointmentKTableProcessor
                                        .getUserAppointments()
                                        .all(),
                                Spliterator.DISTINCT),
                        false)
                .map(entry -> entry.value)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        UserAppointmentKafkaDto::getStart, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public Optional<UserAppointmentKafkaDto> findActiveAppointment(final long id) {
        return findAppointment(id).filter(appointment -> !Boolean.TRUE.equals(appointment.getDeleted()));
    }

    public Optional<UserAppointmentKafkaDto> findAppointment(final long id) {
        return Optional.ofNullable(
                userAppointmentKTableProcessor.getUserAppointments().get(id));
    }
}
