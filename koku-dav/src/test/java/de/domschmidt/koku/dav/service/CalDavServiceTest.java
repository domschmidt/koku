package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.model.DavMultiStatus;
import de.domschmidt.koku.dav.model.DavPropertyNames;
import de.domschmidt.koku.dav.model.DavPropertyRequestType;
import de.domschmidt.koku.dav.model.DavRequest;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CalDavServiceTest {

    private final CustomerAppointmentRepository customerAppointmentRepository =
            mock(CustomerAppointmentRepository.class);
    private final CustomerContactRepository customerContactRepository = mock(CustomerContactRepository.class);
    private final UserAppointmentRepository userAppointmentRepository = mock(UserAppointmentRepository.class);
    private final CalDavService calDavService = new CalDavService(
            customerAppointmentRepository,
            customerContactRepository,
            userAppointmentRepository,
            new CalendarEventFactory());

    @Test
    void returnsNotFoundForForeignPrivateAppointmentMultiget() {
        final UserAppointmentKafkaDto foreignAppointment = UserAppointmentKafkaDto.builder()
                .id(42L)
                .userId("other-user")
                .description("hidden")
                .start(LocalDateTime.of(2026, Month.JUNE, 12, 10, 0))
                .end(LocalDateTime.of(2026, Month.JUNE, 12, 11, 0))
                .updated(LocalDateTime.of(2026, Month.JUNE, 12, 9, 0))
                .build();
        when(userAppointmentRepository.findAllAppointments()).thenReturn(List.of(foreignAppointment));
        when(userAppointmentRepository.findActiveAppointment(42L)).thenReturn(Optional.of(foreignAppointment));

        final DavMultiStatus multiStatus = calDavService.handlePrivateCalendar(
                request("/services/caldav/calendars/current-user/private/42.ics"), "current-user");

        assertThat(multiStatus.responses()).singleElement().satisfies(response -> {
            assertThat(response.href()).isEqualTo("/services/caldav/calendars/current-user/private/42.ics");
            assertThat(response.status()).isEqualTo(404);
            assertThat(response.propStats()).isEmpty();
        });
    }

    private DavRequest request(final String href) {
        return new DavRequest(
                DavMethod.REPORT,
                "/calendars/current-user/private/",
                "/services/caldav",
                null,
                DavPropertyRequestType.NAMED,
                List.of(DavPropertyNames.CALENDAR_DATA),
                List.of(href),
                DavPropertyNames.CALENDAR_MULTIGET,
                null,
                null);
    }
}
