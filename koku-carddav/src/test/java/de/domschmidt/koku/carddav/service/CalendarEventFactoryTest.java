package de.domschmidt.koku.carddav.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class CalendarEventFactoryTest {

    private final CalendarEventFactory factory = new CalendarEventFactory();

    @Test
    void createsValidICalendarEventForAppointments() {
        final String calendar = factory.toICalendar(
                "appointment-42@koku",
                "Facial treatment",
                ZonedDateTime.of(2026, 6, 11, 10, 0, 0, 0, ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(2026, 6, 11, 11, 0, 0, 0, ZoneId.of("Europe/Berlin")));

        assertThat(calendar).contains("BEGIN:VCALENDAR");
        assertThat(calendar).contains("VERSION:2.0");
        assertThat(calendar).contains("PRODID:-//KoKu//Appointments//EN");
        assertThat(calendar).contains("BEGIN:VEVENT");
        assertThat(calendar).contains("UID:appointment-42@koku");
        assertThat(calendar).contains("SUMMARY:Facial treatment");
        assertThat(calendar).contains("END:VEVENT");
        assertThat(calendar).contains("END:VCALENDAR");
    }
}
