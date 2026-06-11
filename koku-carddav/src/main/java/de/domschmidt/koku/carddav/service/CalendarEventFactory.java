package de.domschmidt.koku.carddav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        final ZonedDateTime startsAt = toZonedDateTime(appointment.getStart());
        return toICalendar(
                "customer-appointment-" + appointment.getId() + "@koku",
                summary(appointment),
                startsAt,
                startsAt.plus(DEFAULT_APPOINTMENT_DURATION));
    }

    public String toICalendar(
            final String uid, final String summary, final ZonedDateTime startsAt, final ZonedDateTime endsAt) {
        final Calendar calendar = new Calendar();
        calendar.setPropertyList(new PropertyList()
                .add(new ProdId(PROD_ID))
                .add(new Version(Version.VALUE_2_0, Version.VALUE_2_0))
                .add(new CalScale(CalScale.VALUE_GREGORIAN)));

        final VEvent event = new VEvent(startsAt, endsAt, summary);
        event.setPropertyList(event.getPropertyList().add(new Uid(uid)));
        calendar.setComponentList(new ComponentList<>(java.util.List.of(event)));

        validate(calendar);
        return calendar.toString();
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

    private String summary(final CustomerAppointmentKafkaDto appointment) {
        if (appointment.getDescription() != null
                && !appointment.getDescription().isBlank()) {
            return appointment.getDescription();
        }
        return "KoKu appointment #" + appointment.getId();
    }
}
