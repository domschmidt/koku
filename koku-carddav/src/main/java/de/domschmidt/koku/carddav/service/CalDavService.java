package de.domschmidt.koku.carddav.service;

import de.domschmidt.koku.carddav.APIConstants;
import de.domschmidt.koku.carddav.model.*;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CalDavService {

    private static final String ICALENDAR_CONTENT_TYPE = "text/calendar; charset=utf-8";
    private static final String ICALENDAR_DATA_CONTENT_TYPE = "text/calendar";
    private static final String ICALENDAR_VERSION = "2.0";
    private static final Pattern ICS_ID_PATTERN = Pattern.compile(".*/(\\d+)\\.ics$");

    private final CustomerAppointmentRepository customerAppointmentRepository;
    private final CalendarEventFactory calendarEventFactory;

    public CalDavService(
            final CustomerAppointmentRepository customerAppointmentRepository,
            final CalendarEventFactory calendarEventFactory) {
        this.customerAppointmentRepository = customerAppointmentRepository;
        this.calendarEventFactory = calendarEventFactory;
    }

    public DavMultiStatus handleCalendarHome(final DavRequest request, final String username) {
        final List<DavResponse> responses = new ArrayList<>();
        responses.add(calendarHomeResponse(request, username));
        if (request.depth() != null && request.depth() >= 1) {
            responses.add(calendarCollectionResponse(request, username, syncToken(allAppointments())));
        }
        return new DavMultiStatus(responses);
    }

    public DavMultiStatus handleAppointmentCalendar(final DavRequest request, final String username) {
        final List<CustomerAppointmentKafkaDto> allAppointments = allAppointments();
        final List<CustomerAppointmentKafkaDto> activeAppointments = activeAppointments(allAppointments);
        final List<DavResponse> responses = new ArrayList<>();
        final boolean syncCollectionReport =
                DavMethod.REPORT == request.method() && DavPropertyNames.SYNC_COLLECTION.equals(request.reportName());
        final String currentSyncToken = syncToken(allAppointments);
        if (request.method() == DavMethod.PROPFIND) {
            responses.add(calendarCollectionResponse(request, username, currentSyncToken));
        }
        if (request.method() == DavMethod.REPORT) {
            requireSupportedReport(
                    request.reportName(),
                    DavPropertyNames.CALENDAR_MULTIGET,
                    DavPropertyNames.CALENDAR_QUERY,
                    DavPropertyNames.SYNC_COLLECTION);
        }
        if (syncCollectionReport && currentSyncToken.equals(request.syncToken())) {
            return new DavMultiStatus(responses, currentSyncToken);
        }
        if (syncCollectionReport) {
            allAppointments.stream()
                    .filter(appointment -> Boolean.TRUE.equals(appointment.getDeleted()))
                    .map(appointment -> DavResponse.notFound(
                            appointmentHref(request.hrefBasePath(), username, appointment.getId())))
                    .forEach(responses::add);
        }
        if (request.method() == DavMethod.REPORT || (request.depth() != null && request.depth() >= 1)) {
            final Set<Long> requestedAppointmentIds = parseAppointmentIds(request.hrefs());
            activeAppointments.stream()
                    .filter(appointment ->
                            requestedAppointmentIds.isEmpty() || requestedAppointmentIds.contains(appointment.getId()))
                    .filter(appointment -> matchesTimeRange(appointment, request.timeRange()))
                    .map(appointment -> appointmentResponse(
                            request.hrefBasePath(),
                            username,
                            appointment,
                            request.propertyRequestType(),
                            request.requestedProperties()))
                    .forEach(responses::add);
            if (!syncCollectionReport && !requestedAppointmentIds.isEmpty()) {
                final Set<Long> foundAppointmentIds = new HashSet<>();
                activeAppointments.stream()
                        .map(CustomerAppointmentKafkaDto::getId)
                        .filter(requestedAppointmentIds::contains)
                        .forEach(foundAppointmentIds::add);
                requestedAppointmentIds.stream()
                        .filter(appointmentId -> !foundAppointmentIds.contains(appointmentId))
                        .map(appointmentId ->
                                DavResponse.notFound(appointmentHref(request.hrefBasePath(), username, appointmentId)))
                        .forEach(responses::add);
            }
        }
        if (syncCollectionReport) {
            return new DavMultiStatus(responses, currentSyncToken);
        }
        return new DavMultiStatus(responses);
    }

    public String getICalendar(final long appointmentId) {
        return customerAppointmentRepository
                .findActiveAppointment(appointmentId)
                .map(calendarEventFactory::toICalendar)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    public String getAppointmentEtag(final long appointmentId) {
        return customerAppointmentRepository
                .findActiveAppointment(appointmentId)
                .map(this::etag)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private DavResponse calendarHomeResponse(final DavRequest request, final String username) {
        return new DavResponseBuilder(calendarHomeHref(request.hrefBasePath(), username))
                .property(DavPropertyNames.RESOURCETYPE, new ResourceTypeValue(List.of(DavPropertyNames.COLLECTION)))
                .property(DavPropertyNames.DISPLAYNAME, new TextValue("KoKu Calendars"))
                .property(
                        DavPropertyNames.CURRENT_USER_PRINCIPAL,
                        new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.OWNER, new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.CURRENT_USER_PRIVILEGE_SET, DavCapabilities.readPrivileges())
                .build(request.propertyRequestType(), request.requestedProperties());
    }

    private DavResponse calendarCollectionResponse(
            final DavRequest request, final String username, final String syncToken) {
        return new DavResponseBuilder(appointmentCalendarHref(request.hrefBasePath(), username))
                .property(DavPropertyNames.DISPLAYNAME, new TextValue("KoKu Appointments"))
                .property(DavPropertyNames.CALENDAR_DESCRIPTION, new TextValue("KoKu customer appointments"))
                .property(
                        DavPropertyNames.RESOURCETYPE,
                        new ResourceTypeValue(List.of(DavPropertyNames.COLLECTION, DavPropertyNames.CALENDAR)))
                .property(
                        DavPropertyNames.CURRENT_USER_PRINCIPAL,
                        new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.OWNER, new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.CURRENT_USER_PRIVILEGE_SET, DavCapabilities.readPrivileges())
                .property(DavPropertyNames.SUPPORTED_REPORT_SET, supportedReports())
                .property(DavPropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET, supportedCalendarComponents())
                .property(DavPropertyNames.SUPPORTED_CALENDAR_DATA, supportedCalendarData())
                .property(DavPropertyNames.CALENDAR_SUPPORTED_COLLATION_SET, DavCapabilities.supportedCollations())
                .property(
                        DavPropertyNames.CALENDAR_MAX_RESOURCE_SIZE,
                        new TextValue(DavResourceMetadata.MAX_RESOURCE_SIZE_BYTES))
                .property(DavPropertyNames.SYNC_TOKEN, new TextValue(syncToken))
                .property(DavPropertyNames.GETCTAG, new TextValue(syncToken))
                .build(request.propertyRequestType(), request.requestedProperties());
    }

    private DavResponse appointmentResponse(
            final String hrefBasePath,
            final String username,
            final CustomerAppointmentKafkaDto appointment,
            final DavPropertyRequestType propertyRequestType,
            final List<DavPropertyName> requestedProperties) {
        final String iCalendar = calendarEventFactory.toICalendar(appointment);
        return new DavResponseBuilder(appointmentHref(hrefBasePath, username, appointment.getId()))
                .property(DavPropertyNames.GETETAG, new TextValue(etag(appointment)))
                .property(DavPropertyNames.GETCONTENTTYPE, new TextValue(ICALENDAR_CONTENT_TYPE))
                .property(
                        DavPropertyNames.GETLASTMODIFIED,
                        new TextValue(DavResourceMetadata.lastModified(appointment.getUpdated())))
                .property(
                        DavPropertyNames.GETCONTENTLENGTH,
                        new TextValue(String.valueOf(DavResourceMetadata.byteLength(iCalendar))))
                .property(DavPropertyNames.CALENDAR_DATA, new CalendarDataValue(iCalendar))
                .build(propertyRequestType, requestedProperties);
    }

    private Set<Long> parseAppointmentIds(final List<String> hrefs) {
        final Set<Long> appointmentIds = new HashSet<>();
        for (final String href : hrefs) {
            final Matcher matcher = ICS_ID_PATTERN.matcher(href);
            if (matcher.find()) {
                appointmentIds.add(Long.parseLong(matcher.group(1)));
            }
        }
        return appointmentIds;
    }

    private List<CustomerAppointmentKafkaDto> allAppointments() {
        return customerAppointmentRepository.findAllAppointments();
    }

    private List<CustomerAppointmentKafkaDto> activeAppointments(final List<CustomerAppointmentKafkaDto> appointments) {
        return appointments.stream()
                .filter(appointment -> !Boolean.TRUE.equals(appointment.getDeleted()))
                .toList();
    }

    private void requireSupportedReport(final DavPropertyName reportName, final DavPropertyName... supportedReports) {
        if (reportName == null || java.util.Arrays.stream(supportedReports).noneMatch(reportName::equals)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported CalDAV REPORT");
        }
    }

    private boolean matchesTimeRange(final CustomerAppointmentKafkaDto appointment, final DavTimeRange timeRange) {
        if (timeRange == null || appointment.getStart() == null) {
            return true;
        }
        final Instant startsAt = toInstant(appointment.getStart());
        final Instant endsAt = startsAt.plus(CalendarEventFactory.DEFAULT_APPOINTMENT_DURATION);
        return timeRange.overlaps(startsAt, endsAt);
    }

    private Instant toInstant(final LocalDateTime localDateTime) {
        return localDateTime.atZone(CalendarEventFactory.DEFAULT_ZONE).toInstant();
    }

    private SupportedReportSetValue supportedReports() {
        return new SupportedReportSetValue(List.of(
                DavPropertyNames.CALENDAR_MULTIGET, DavPropertyNames.CALENDAR_QUERY, DavPropertyNames.SYNC_COLLECTION));
    }

    private SupportedCalendarComponentSetValue supportedCalendarComponents() {
        return new SupportedCalendarComponentSetValue(List.of(DavPropertyNames.VEVENT));
    }

    private SupportedCalendarDataValue supportedCalendarData() {
        return new SupportedCalendarDataValue(
                List.of(new CalendarDataType(ICALENDAR_DATA_CONTENT_TYPE, ICALENDAR_VERSION)));
    }

    private String syncToken(final List<CustomerAppointmentKafkaDto> appointments) {
        final int token = appointments.stream()
                .map(appointment ->
                        Objects.hash(appointment.getId(), appointment.getUpdated(), appointment.getDeleted()))
                .sorted()
                .reduce(1, (left, right) -> 31 * left + right);
        return "urn:koku:caldav:sync:" + Integer.toUnsignedString(token);
    }

    private String etag(final CustomerAppointmentKafkaDto appointment) {
        return DavResourceMetadata.etag(appointment.getId(), appointment.getUpdated());
    }

    private String principalHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.PRINCIPALS_PATH + "/" + username + "/";
    }

    private String calendarHomeHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.CALENDAR_PATH + "/" + username + "/";
    }

    private String appointmentCalendarHref(final String hrefBasePath, final String username) {
        return calendarHomeHref(hrefBasePath, username) + "appointments/";
    }

    private String appointmentHref(final String hrefBasePath, final String username, final long appointmentId) {
        return appointmentCalendarHref(hrefBasePath, username) + appointmentId + ".ics";
    }
}
