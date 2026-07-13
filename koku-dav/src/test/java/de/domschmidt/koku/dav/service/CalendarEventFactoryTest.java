package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationException;
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
    void createsCustomerAppointmentWithFallbackNameAndDefaultStart() {
        final String calendar = factory.toICalendar(
                CustomerAppointmentKafkaDto.builder().id(44L).build(),
                Optional.of(CustomerKafkaDto.builder()
                        .fullname(" ")
                        .firstname(" Ada ")
                        .lastname("Lovelace")
                        .build()));

        assertThat(calendar).contains("UID:customer-appointment-44@koku", "SUMMARY:Kundentermin - Ada  Lovelace");
    }

    @Test
    void createsCustomerAppointmentWithoutCustomerAndPrivateAppointmentWithDefaultEnd() {
        final String customerCalendar = factory.toICalendar(CustomerAppointmentKafkaDto.builder()
                .id(45L)
                .start(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0))
                .build());
        final String privateCalendar = factory.toICalendar(UserAppointmentKafkaDto.builder()
                .id(46L)
                .start(LocalDateTime.of(2026, Month.JUNE, 11, 10, 0))
                .build());

        assertThat(customerCalendar).contains("SUMMARY:Kundentermin");
        assertThat(privateCalendar)
                .contains("SUMMARY:Privater Termin", "DTSTART:20260611T080000Z", "DTEND:20260611T090000Z");
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

    @Test
    void serializesSummerAppointmentAsUnambiguousUtcInstantsForIos() {
        final String calendar = factory.toICalendar(
                "summer-appointment@koku",
                "Summer appointment",
                ZonedDateTime.of(LocalDateTime.of(2026, Month.JULY, 15, 10, 0), ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(LocalDateTime.of(2026, Month.JULY, 15, 11, 0), ZoneId.of("Europe/Berlin")));

        assertThat(calendar).contains("DTSTART:20260715T080000Z", "DTEND:20260715T090000Z");
    }

    @Test
    void serializesWinterAppointmentAsUnambiguousUtcInstantsForIos() {
        final String calendar = factory.toICalendar(
                "winter-appointment@koku",
                "Winter appointment",
                ZonedDateTime.of(LocalDateTime.of(2026, Month.JANUARY, 15, 10, 0), ZoneId.of("Europe/Berlin")),
                ZonedDateTime.of(LocalDateTime.of(2026, Month.JANUARY, 15, 11, 0), ZoneId.of("Europe/Berlin")));

        assertThat(calendar).contains("DTSTART:20260115T090000Z", "DTEND:20260115T100000Z");
    }

    @Test
    void invalidGeneratedCalendarIsRejected() throws Exception {
        final Calendar calendar = mock(Calendar.class);
        doThrow(new ValidationException("invalid")).when(calendar).validate();

        assertThatThrownBy(() -> factory.validate(calendar)).isInstanceOf(IllegalArgumentException.class);
    }
}
