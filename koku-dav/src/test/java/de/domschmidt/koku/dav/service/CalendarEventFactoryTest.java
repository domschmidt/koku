package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CalendarEventFactoryTest {

    private final CalendarEventFactory factory = new CalendarEventFactory();

    @Test
    void createsValidICalendarEventForAppointments() {
        final String calendar = factory.toICalendar(
                "appointment-42@koku",
                "Facial treatment",
                ZonedDateTime.of(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0), ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(LocalDateTime.of(2026, Month.JUNE, 11, 11, 0), ZoneId.of("Europe/Berlin")));

        assertThat(calendar)
                .contains(
                        "BEGIN:VCALENDAR",
                        "VERSION:2.0",
                        "PRODID:-//KoKu//Appointments//EN",
                        "BEGIN:VEVENT",
                        "UID:appointment-42@koku",
                        "SUMMARY:Facial treatment",
                        "END:VEVENT",
                        "END:VCALENDAR")
                .doesNotContain("VERSION:2.0;2.0");
    }

    @Test
    void createsCustomerAppointmentSummaryFromCustomerDescriptionAndAdditionalInfo() {
        final String calendar = factory.toICalendar(
                CustomerAppointmentKafkaDto.builder()
                        .id(42L)
                        .description("Neuanlage")
                        .additionalInfo("Patch Test beachten")
                        .start(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0))
                        .build(),
                Optional.of(
                        CustomerKafkaDto.builder().fullname("Max Mustermann").build()));

        assertThat(calendar).contains("SUMMARY:Kundentermin - Max Mustermann - Neuanlage - Patch Test beachten");
    }

    @Test
    void createsPrivateAppointmentSummaryFromLabelAndDescription() {
        final String calendar = factory.toICalendar(UserAppointmentKafkaDto.builder()
                .id(43L)
                .description("Arzt")
                .start(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0))
                .build());

        assertThat(calendar).contains("SUMMARY:Privater Termin - Arzt");
    }
}
