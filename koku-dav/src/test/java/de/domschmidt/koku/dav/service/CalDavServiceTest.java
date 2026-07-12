package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
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
    void includesCustomerAppointmentWhenCalculatedEndOverlapsTimeRange() {
        final CustomerAppointmentKafkaDto appointment =
                customerAppointment(LocalDateTime.of(2026, Month.JULY, 15, 12, 30));
        when(customerAppointmentRepository.findAllAppointments()).thenReturn(List.of(appointment));

        final DavMultiStatus result = calDavService.handleAppointmentCalendar(
                customerQueryRequest(
                        new DavTimeRange(Instant.parse("2026-07-15T10:00:00Z"), Instant.parse("2026-07-15T11:00:00Z"))),
                USERNAME);

        assertThat(result.responses())
                .extracting(DavResponse::href)
                .containsExactly("/services/caldav/calendars/current-user/appointments/42.ics");
    }

    @Test
    void excludesCustomerAppointmentWithoutCalculatedEndAfterDefaultDuration() {
        final CustomerAppointmentKafkaDto appointment = customerAppointment(null);
        when(customerAppointmentRepository.findAllAppointments()).thenReturn(List.of(appointment));

        final DavMultiStatus result = calDavService.handleAppointmentCalendar(
                customerQueryRequest(
                        new DavTimeRange(Instant.parse("2026-07-15T09:30:00Z"), Instant.parse("2026-07-15T10:30:00Z"))),
                USERNAME);

        assertThat(result.responses()).isEmpty();
    }

    private CustomerAppointmentKafkaDto customerAppointment(final LocalDateTime end) {
        return CustomerAppointmentKafkaDto.builder()
                .id(42L)
                .userId(USERNAME)
                .start(LocalDateTime.of(2026, Month.JULY, 15, 10, 0))
                .end(end)
                .updated(LocalDateTime.of(2026, Month.JULY, 1, 12, 0))
                .deleted(false)
                .build();
    }

    private DavRequest customerQueryRequest(final DavTimeRange timeRange) {
        return new DavRequest(
                DavMethod.REPORT,
                "/calendars/current-user/appointments/",
                "/services/caldav",
                1,
                DavPropertyRequestType.NAMED,
                List.of(DavPropertyNames.GETETAG),
                List.of(),
                DavPropertyNames.CALENDAR_QUERY,
                timeRange,
                null);
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
