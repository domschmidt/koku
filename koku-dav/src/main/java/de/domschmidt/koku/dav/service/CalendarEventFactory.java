package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.validate.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class CalendarEventFactory {

    private static final String PROD_ID = "-//KoKu//Appointments//EN";
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Berlin");
    public static final Duration DEFAULT_APPOINTMENT_DURATION = Duration.ofHours(1);

    public String toICalendar(final CustomerAppointmentKafkaDto appointment) {
        return toICalendar(appointment, Optional.empty());
    }

    public String toICalendar(
            final CustomerAppointmentKafkaDto appointment, final Optional<CustomerKafkaDto> customer) {
        final ZonedDateTime startsAt = toZonedDateTime(appointment.getStart());
        final ZonedDateTime endsAt = appointment.getEnd() == null
                ? startsAt.plus(DEFAULT_APPOINTMENT_DURATION)
                : toZonedDateTime(appointment.getEnd());
        return toICalendar(
                "customer-appointment-" + appointment.getId() + "@koku",
                summary(appointment, customer),
                startsAt,
                endsAt);
    }

    public String toICalendar(
            final String uid, final String summary, final ZonedDateTime startsAt, final ZonedDateTime endsAt) {
        final Calendar calendar = new Calendar();
        final Version version = new Version();
        version.setValue(Version.VALUE_2_0);
        calendar.setPropertyList(
                new PropertyList().add(new ProdId(PROD_ID)).add(version).add(new CalScale(CalScale.VALUE_GREGORIAN)));

        final VEvent event = new VEvent(startsAt.toInstant(), endsAt.toInstant(), summary);
        event.setPropertyList(event.getPropertyList().add(new Uid(uid)));
        calendar.setComponentList(new ComponentList<>(java.util.List.of(event)));

        validate(calendar);
        return calendar.toString();
    }

    public String toICalendar(final UserAppointmentKafkaDto appointment) {
        final ZonedDateTime startsAt = toZonedDateTime(appointment.getStart());
        final ZonedDateTime endsAt = appointment.getEnd() == null
                ? startsAt.plus(DEFAULT_APPOINTMENT_DURATION)
                : toZonedDateTime(appointment.getEnd());
        return toICalendar("user-appointment-" + appointment.getId() + "@koku", summary(appointment), startsAt, endsAt);
    }

    private void validate(final Calendar calendar) {
        try {
            calendar.validate();
        } catch (final ValidationException e) {
            throw new IllegalArgumentException("Generated iCalendar data is invalid", e);
        }
    }

    private ZonedDateTime toZonedDateTime(final LocalDateTime localDateTime) {
        return (localDateTime == null ? LocalDateTime.now(DEFAULT_ZONE) : localDateTime).atZone(DEFAULT_ZONE);
    }

    private String summary(final CustomerAppointmentKafkaDto appointment, final Optional<CustomerKafkaDto> customer) {
        final List<String> parts = new ArrayList<>();
        parts.add("Kundentermin");
        customer.map(this::customerName).filter(name -> !name.isBlank()).ifPresent(parts::add);
        addIfNotBlank(parts, appointment.getDescription());
        addIfNotBlank(parts, appointment.getAdditionalInfo());
        return String.join(" - ", parts);
    }

    private String customerName(final CustomerKafkaDto customer) {
        if (customer.getFullname() != null && !customer.getFullname().isBlank()) {
            return customer.getFullname();
        }
        return String.join(
                        " ",
                        List.of(
                                Optional.ofNullable(customer.getFirstname()).orElse(""),
                                Optional.ofNullable(customer.getLastname()).orElse("")))
                .trim();
    }

    private void addIfNotBlank(final List<String> parts, final String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value.trim());
        }
    }

    private String summary(final UserAppointmentKafkaDto appointment) {
        final List<String> parts = new ArrayList<>();
        parts.add("Privater Termin");
        addIfNotBlank(parts, appointment.getDescription());
        return String.join(" - ", parts);
    }
}
