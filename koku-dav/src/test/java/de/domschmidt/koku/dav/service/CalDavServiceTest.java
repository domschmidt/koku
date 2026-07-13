package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.model.DavMultiStatus;
import de.domschmidt.koku.dav.model.DavPropertyNames;
import de.domschmidt.koku.dav.model.DavPropertyRequestType;
import de.domschmidt.koku.dav.model.DavRequest;
import de.domschmidt.koku.dav.model.DavResponse;
import de.domschmidt.koku.dav.model.DavTimeRange;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CalDavServiceTest {

    private static final String USERNAME = "current-user";
    private static final String PRIVATE_APPOINTMENT_HREF = "/services/caldav/calendars/current-user/private/42.ics";

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

    @Test
    void reportsDeletedCustomerAppointmentExactlyOnceDuringIncrementalSync() {
        final CustomerAppointmentKafkaDto activeAppointment = CustomerAppointmentKafkaDto.builder()
                .id(42L)
                .userId(USERNAME)
                .start(LocalDateTime.of(2026, Month.JULY, 15, 10, 0))
                .updated(LocalDateTime.of(2026, Month.JULY, 1, 12, 0))
                .deleted(false)
                .build();
        final CustomerAppointmentKafkaDto deletedAppointment = CustomerAppointmentKafkaDto.builder()
                .id(42L)
                .userId(USERNAME)
                .start(activeAppointment.getStart())
                .updated(LocalDateTime.of(2026, Month.JULY, 2, 12, 0))
                .deleted(true)
                .build();
        when(customerAppointmentRepository.findAllAppointments())
                .thenReturn(List.of(activeAppointment), List.of(deletedAppointment), List.of(deletedAppointment));

        final DavMultiStatus initialSync = calDavService.handleAppointmentCalendar(syncRequest(null), USERNAME);
        final DavMultiStatus deletionSync =
                calDavService.handleAppointmentCalendar(syncRequest(initialSync.syncToken()), USERNAME);
        final DavMultiStatus unchangedSync =
                calDavService.handleAppointmentCalendar(syncRequest(deletionSync.syncToken()), USERNAME);

        assertThat(initialSync.responses())
                .extracting(DavResponse::href)
                .containsExactly("/services/caldav/calendars/current-user/appointments/42.ics");
        assertThat(deletionSync.syncToken()).isNotEqualTo(initialSync.syncToken());
        assertThat(deletionSync.responses()).singleElement().satisfies(response -> {
            assertThat(response.href()).isEqualTo("/services/caldav/calendars/current-user/appointments/42.ics");
            assertThat(response.status()).isEqualTo(404);
            assertThat(response.propStats()).isEmpty();
        });
        assertThat(unchangedSync.syncToken()).isEqualTo(deletionSync.syncToken());
        assertThat(unchangedSync.responses()).isEmpty();
    }

    @Test
    void reportsDeletedPrivateAppointmentExactlyOnceDuringIncrementalSync() {
        final UserAppointmentKafkaDto activeAppointment = privateAppointment(false, 1);
        final UserAppointmentKafkaDto deletedAppointment = privateAppointment(true, 2);
        when(userAppointmentRepository.findAllAppointments())
                .thenReturn(List.of(activeAppointment), List.of(deletedAppointment), List.of(deletedAppointment));

        final DavMultiStatus initialSync = calDavService.handlePrivateCalendar(privateSyncRequest(null), USERNAME);
        final DavMultiStatus deletionSync =
                calDavService.handlePrivateCalendar(privateSyncRequest(initialSync.syncToken()), USERNAME);
        final DavMultiStatus unchangedSync =
                calDavService.handlePrivateCalendar(privateSyncRequest(deletionSync.syncToken()), USERNAME);

        assertThat(initialSync.responses()).extracting(DavResponse::href).containsExactly(PRIVATE_APPOINTMENT_HREF);
        assertThat(deletionSync.syncToken()).isNotEqualTo(initialSync.syncToken());
        assertThat(deletionSync.responses()).singleElement().satisfies(response -> {
            assertThat(response.href()).isEqualTo(PRIVATE_APPOINTMENT_HREF);
            assertThat(response.status()).isEqualTo(404);
            assertThat(response.propStats()).isEmpty();
        });
        assertThat(unchangedSync.syncToken()).isEqualTo(deletionSync.syncToken());
        assertThat(unchangedSync.responses()).isEmpty();
    }

    @Test
    void calendarHomeAndEmptyCollectionsExposeStablePropfindResources() {
        when(customerAppointmentRepository.findAllAppointments()).thenReturn(List.of());
        when(userAppointmentRepository.findAllAppointments()).thenReturn(List.of());
        final DavRequest propfind = new DavRequest(
                DavMethod.PROPFIND,
                "/calendars/current-user/",
                "/services/caldav",
                1,
                DavPropertyRequestType.ALL,
                List.of(),
                List.of(),
                null,
                null,
                null);

        assertThat(calDavService.handleCalendarHome(propfind, USERNAME).responses())
                .hasSize(3);
        assertThat(calDavService.handleAppointmentCalendar(propfind, USERNAME).responses())
                .hasSize(1);
        assertThat(calDavService.handlePrivateCalendar(propfind, USERNAME).responses())
                .hasSize(1);
    }

    @Test
    void unsupportedReportsAndForeignDirectReadsAreRejected() {
        when(customerAppointmentRepository.findAllAppointments()).thenReturn(List.of());
        when(userAppointmentRepository.findAllAppointments()).thenReturn(List.of());
        final DavRequest unsupported = new DavRequest(
                DavMethod.REPORT,
                "/calendars/current-user/appointments/",
                "/services/caldav",
                1,
                DavPropertyRequestType.NAMED,
                List.of(),
                List.of(),
                DavPropertyNames.ADDRESSBOOK_QUERY,
                null,
                null);

        assertThatThrownBy(() -> calDavService.handleAppointmentCalendar(unsupported, USERNAME))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> calDavService.handlePrivateCalendar(unsupported, USERNAME))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> calDavService.getICalendar(USERNAME, 99L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> calDavService.getAppointmentEtag(USERNAME, 99L))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> calDavService.getPrivateICalendar(USERNAME, 99L))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> calDavService.getPrivateAppointmentEtag(USERNAME, 99L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void activeCalendarsSupportPropfindQueriesMultigetAndDirectReads() {
        final CustomerAppointmentKafkaDto customerAppointment = CustomerAppointmentKafkaDto.builder()
                .id(42L)
                .customerId(7L)
                .userId(USERNAME)
                .start(LocalDateTime.of(2026, Month.JULY, 15, 10, 0))
                .updated(LocalDateTime.of(2026, Month.JULY, 14, 10, 0))
                .build();
        final CustomerKafkaDto customer = CustomerKafkaDto.builder()
                .id(7L)
                .fullname("Ada Lovelace")
                .updated(LocalDateTime.of(2026, Month.JULY, 14, 11, 0))
                .build();
        final UserAppointmentKafkaDto privateAppointment = privateAppointment(false, 14);
        when(customerAppointmentRepository.findAllAppointments()).thenReturn(List.of(customerAppointment));
        when(customerAppointmentRepository.findActiveAppointment(42L)).thenReturn(Optional.of(customerAppointment));
        when(customerContactRepository.findActiveContact(7L)).thenReturn(Optional.of(customer));
        when(userAppointmentRepository.findAllAppointments()).thenReturn(List.of(privateAppointment));
        when(userAppointmentRepository.findActiveAppointment(42L)).thenReturn(Optional.of(privateAppointment));

        final DavRequest propfind = new DavRequest(
                DavMethod.PROPFIND,
                "/calendars/current-user/appointments/",
                "/services/caldav",
                1,
                DavPropertyRequestType.ALL,
                List.of(),
                List.of(),
                null,
                null,
                null);
        final DavRequest query = new DavRequest(
                DavMethod.REPORT,
                "/calendars/current-user/appointments/",
                "/services/caldav",
                1,
                DavPropertyRequestType.NAMED,
                List.of(DavPropertyNames.GETETAG, DavPropertyNames.CALENDAR_DATA),
                List.of("/services/caldav/calendars/current-user/appointments/42.ics"),
                DavPropertyNames.CALENDAR_QUERY,
                new DavTimeRange(Instant.parse("2026-07-15T00:00:00Z"), Instant.parse("2026-07-16T00:00:00Z")),
                null);

        assertThat(calDavService.handleAppointmentCalendar(propfind, USERNAME).responses())
                .hasSize(2);
        assertThat(calDavService.handlePrivateCalendar(propfind, USERNAME).responses())
                .hasSize(2);
        assertThat(calDavService.handleAppointmentCalendar(query, USERNAME).responses())
                .singleElement();
        assertThat(calDavService.handlePrivateCalendar(query, USERNAME).responses())
                .singleElement();
        assertThat(calDavService
                        .handleAppointmentCalendar(
                                request("/services/caldav/calendars/current-user/appointments/99.ics"), USERNAME)
                        .responses())
                .singleElement()
                .extracting(DavResponse::status)
                .isEqualTo(404);
        final UserAppointmentKafkaDto appointmentWithoutStart =
                UserAppointmentKafkaDto.builder().build();
        assertThat(calDavService.matchesTimeRange(appointmentWithoutStart, query.timeRange()))
                .isTrue();
        final UserAppointmentKafkaDto appointmentWithoutEnd = UserAppointmentKafkaDto.builder()
                .start(LocalDateTime.of(2026, Month.JULY, 15, 10, 0))
                .build();
        assertThat(calDavService.matchesTimeRange(appointmentWithoutEnd, query.timeRange()))
                .isTrue();
        assertThat(calDavService.getICalendar(USERNAME, 42L)).contains("Ada Lovelace");
        assertThat(calDavService.getAppointmentEtag(USERNAME, 42L)).isNotBlank();
        assertThat(calDavService.getPrivateICalendar(USERNAME, 42L)).contains("BEGIN:VCALENDAR");
        assertThat(calDavService.getPrivateAppointmentEtag(USERNAME, 42L)).isNotBlank();
    }

    private UserAppointmentKafkaDto privateAppointment(final boolean deleted, final int updatedDay) {
        return UserAppointmentKafkaDto.builder()
                .id(42L)
                .userId(USERNAME)
                .description("iOS sync test")
                .start(LocalDateTime.of(2026, Month.JULY, 15, 10, 0))
                .end(LocalDateTime.of(2026, Month.JULY, 15, 11, 0))
                .updated(LocalDateTime.of(2026, Month.JULY, updatedDay, 12, 0))
                .deleted(deleted)
                .build();
    }

    private DavRequest syncRequest(final String syncToken) {
        return new DavRequest(
                DavMethod.REPORT,
                "/calendars/current-user/appointments/",
                "/services/caldav",
                1,
                DavPropertyRequestType.NAMED,
                List.of(DavPropertyNames.GETETAG),
                List.of(),
                DavPropertyNames.SYNC_COLLECTION,
                null,
                syncToken);
    }

    private DavRequest privateSyncRequest(final String syncToken) {
        return new DavRequest(
                DavMethod.REPORT,
                "/calendars/current-user/private/",
                "/services/caldav",
                1,
                DavPropertyRequestType.NAMED,
                List.of(DavPropertyNames.GETETAG),
                List.of(),
                DavPropertyNames.SYNC_COLLECTION,
                null,
                syncToken);
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
