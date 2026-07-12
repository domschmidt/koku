package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dav.APIConstants;
import de.domschmidt.koku.dav.http.DavMediaTypes;
import de.domschmidt.koku.dav.model.*;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
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

    private static final String CUSTOMER_APPOINTMENT_SYNC_STATE_PREFIX = "urn:koku:caldav:sync:";
    private static final String PRIVATE_APPOINTMENT_SYNC_STATE_PREFIX = "urn:koku:caldav:private:sync:";
    private static final Pattern ICS_ID_PATTERN = Pattern.compile("/(\\d+)\\.ics$");

    private final CustomerAppointmentRepository customerAppointmentRepository;
    private final CustomerContactRepository customerContactRepository;
    private final UserAppointmentRepository userAppointmentRepository;
    private final CalendarEventFactory calendarEventFactory;

    public CalDavService(
            final CustomerAppointmentRepository customerAppointmentRepository,
            final CustomerContactRepository customerContactRepository,
            final UserAppointmentRepository userAppointmentRepository,
            final CalendarEventFactory calendarEventFactory) {
        this.customerAppointmentRepository = customerAppointmentRepository;
        this.customerContactRepository = customerContactRepository;
        this.userAppointmentRepository = userAppointmentRepository;
        this.calendarEventFactory = calendarEventFactory;
    }

    public DavMultiStatus handleCalendarHome(final DavRequest request, final String username) {
        final List<DavResponse> responses = new ArrayList<>();
        responses.add(calendarHomeResponse(request, username));
        if (request.depth() != null && request.depth() >= 1) {
            responses.add(calendarCollectionResponse(
                    request,
                    username,
                    appointmentCalendarHref(request.hrefBasePath(), username),
                    "KoKu Appointments",
                    "KoKu customer appointments",
                    customerAppointmentSyncToken(allAppointments(username))));
            responses.add(calendarCollectionResponse(
                    request,
                    username,
                    privateCalendarHref(request.hrefBasePath(), username),
                    "KoKu Private",
                    "KoKu private appointments",
                    userAppointmentSyncToken(allUserAppointments(username))));
        }
        return new DavMultiStatus(responses);
    }

    public DavMultiStatus handleAppointmentCalendar(final DavRequest request, final String username) {
        final List<CustomerAppointmentKafkaDto> allAppointments = allAppointments(username);
        final List<CustomerAppointmentKafkaDto> activeAppointments = activeAppointments(allAppointments);
        final List<DavResponse> responses = new ArrayList<>();
        final boolean syncCollectionReport =
                DavMethod.REPORT == request.method() && DavPropertyNames.SYNC_COLLECTION.equals(request.reportName());
        final String currentSyncToken = customerAppointmentSyncToken(allAppointments);
        if (request.method() == DavMethod.PROPFIND) {
            responses.add(calendarCollectionResponse(
                    request,
                    username,
                    appointmentCalendarHref(request.hrefBasePath(), username),
                    "KoKu Appointments",
                    "KoKu customer appointments",
                    currentSyncToken));
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
            if (requestedAppointmentIds.isEmpty()) {
                activeAppointments.stream()
                        .filter(appointment -> matchesTimeRange(appointment, request.timeRange()))
                        .map(appointment -> appointmentResponse(
                                request.hrefBasePath(),
                                username,
                                appointment,
                                request.propertyRequestType(),
                                request.requestedProperties()))
                        .forEach(responses::add);
            } else {
                requestedAppointmentIds.stream()
                        .map(appointmentId -> customerAppointmentRepository
                                .findActiveAppointment(appointmentId)
                                .filter(appointment -> belongsToUser(appointment, username))
                                .filter(appointment -> matchesTimeRange(appointment, request.timeRange()))
                                .map(appointment -> appointmentResponse(
                                        request.hrefBasePath(),
                                        username,
                                        appointment,
                                        request.propertyRequestType(),
                                        request.requestedProperties()))
                                .orElseGet(() -> DavResponse.notFound(
                                        appointmentHref(request.hrefBasePath(), username, appointmentId))))
                        .forEach(responses::add);
            }
        }
        if (syncCollectionReport) {
            return new DavMultiStatus(responses, currentSyncToken);
        }
        return new DavMultiStatus(responses);
    }

    public DavMultiStatus handlePrivateCalendar(final DavRequest request, final String username) {
        final List<UserAppointmentKafkaDto> allAppointments = allUserAppointments(username);
        final List<UserAppointmentKafkaDto> activeAppointments = activeUserAppointments(allAppointments);
        final List<DavResponse> responses = new ArrayList<>();
        final boolean syncCollectionReport =
                DavMethod.REPORT == request.method() && DavPropertyNames.SYNC_COLLECTION.equals(request.reportName());
        final String currentSyncToken = userAppointmentSyncToken(allAppointments);
        if (request.method() == DavMethod.PROPFIND) {
            responses.add(calendarCollectionResponse(
                    request,
                    username,
                    privateCalendarHref(request.hrefBasePath(), username),
                    "KoKu Private",
                    "KoKu private appointments",
                    currentSyncToken));
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
                            privateAppointmentHref(request.hrefBasePath(), username, appointment.getId())))
                    .forEach(responses::add);
        }
        if (request.method() == DavMethod.REPORT || (request.depth() != null && request.depth() >= 1)) {
            final Set<Long> requestedAppointmentIds = parseAppointmentIds(request.hrefs());
            if (requestedAppointmentIds.isEmpty()) {
                activeAppointments.stream()
                        .filter(appointment -> matchesTimeRange(appointment, request.timeRange()))
                        .map(appointment -> privateAppointmentResponse(
                                request.hrefBasePath(),
                                username,
                                appointment,
                                request.propertyRequestType(),
                                request.requestedProperties()))
                        .forEach(responses::add);
            } else {
                requestedAppointmentIds.stream()
                        .map(appointmentId -> userAppointmentRepository
                                .findActiveAppointment(appointmentId)
                                .filter(appointment -> belongsToUser(appointment, username))
                                .filter(appointment -> matchesTimeRange(appointment, request.timeRange()))
                                .map(appointment -> privateAppointmentResponse(
                                        request.hrefBasePath(),
                                        username,
                                        appointment,
                                        request.propertyRequestType(),
                                        request.requestedProperties()))
                                .orElseGet(() -> DavResponse.notFound(
                                        privateAppointmentHref(request.hrefBasePath(), username, appointmentId))))
                        .forEach(responses::add);
            }
        }
        if (syncCollectionReport) {
            return new DavMultiStatus(responses, currentSyncToken);
        }
        return new DavMultiStatus(responses);
    }

    public String getICalendar(final String username, final long appointmentId) {
        return customerAppointmentRepository
                .findActiveAppointment(appointmentId)
                .filter(appointment -> belongsToUser(appointment, username))
                .map(appointment -> calendarEventFactory.toICalendar(appointment, customer(appointment)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    public String getAppointmentEtag(final String username, final long appointmentId) {
        return customerAppointmentRepository
                .findActiveAppointment(appointmentId)
                .filter(appointment -> belongsToUser(appointment, username))
                .map(appointment -> customerAppointmentEtag(appointment, customer(appointment)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    public String getPrivateICalendar(final String username, final long appointmentId) {
        return userAppointmentRepository
                .findActiveAppointment(appointmentId)
                .filter(appointment -> belongsToUser(appointment, username))
                .map(calendarEventFactory::toICalendar)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Private appointment not found"));
    }

    public String getPrivateAppointmentEtag(final String username, final long appointmentId) {
        return userAppointmentRepository
                .findActiveAppointment(appointmentId)
                .filter(appointment -> belongsToUser(appointment, username))
                .map(this::etag)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Private appointment not found"));
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
            final DavRequest request,
            final String username,
            final String href,
            final String displayName,
            final String description,
            final String syncToken) {
        return new DavResponseBuilder(href)
                .property(DavPropertyNames.DISPLAYNAME, new TextValue(displayName))
                .property(DavPropertyNames.CALENDAR_DESCRIPTION, new TextValue(description))
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
        final String iCalendar = calendarEventFactory.toICalendar(appointment, customer(appointment));
        final java.util.Optional<CustomerKafkaDto> customer = customer(appointment);
        return new DavResponseBuilder(appointmentHref(hrefBasePath, username, appointment.getId()))
                .property(DavPropertyNames.GETETAG, new TextValue(customerAppointmentEtag(appointment, customer)))
                .property(DavPropertyNames.GETCONTENTTYPE, new TextValue(DavMediaTypes.ICALENDAR_UTF8))
                .property(
                        DavPropertyNames.GETLASTMODIFIED,
                        new TextValue(DavResourceMetadata.lastModified(
                                customerAppointmentLastModified(appointment, customer))))
                .property(
                        DavPropertyNames.GETCONTENTLENGTH,
                        new TextValue(String.valueOf(DavResourceMetadata.byteLength(iCalendar))))
                .property(DavPropertyNames.CALENDAR_DATA, new CalendarDataValue(iCalendar))
                .build(propertyRequestType, requestedProperties);
    }

    private DavResponse privateAppointmentResponse(
            final String hrefBasePath,
            final String username,
            final UserAppointmentKafkaDto appointment,
            final DavPropertyRequestType propertyRequestType,
            final List<DavPropertyName> requestedProperties) {
        final String iCalendar = calendarEventFactory.toICalendar(appointment);
        return new DavResponseBuilder(privateAppointmentHref(hrefBasePath, username, appointment.getId()))
                .property(DavPropertyNames.GETETAG, new TextValue(etag(appointment)))
                .property(DavPropertyNames.GETCONTENTTYPE, new TextValue(DavMediaTypes.ICALENDAR_UTF8))
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

    private java.util.Optional<CustomerKafkaDto> customer(final CustomerAppointmentKafkaDto appointment) {
        if (appointment.getCustomerId() == null) {
            return java.util.Optional.empty();
        }
        return customerContactRepository.findActiveContact(appointment.getCustomerId());
    }

    private List<CustomerAppointmentKafkaDto> allAppointments(final String username) {
        return customerAppointmentRepository.findAllAppointments().stream()
                .filter(appointment -> belongsToUser(appointment, username))
                .toList();
    }

    private List<UserAppointmentKafkaDto> allUserAppointments(final String username) {
        return userAppointmentRepository.findAllAppointments().stream()
                .filter(appointment -> belongsToUser(appointment, username))
                .toList();
    }

    private List<CustomerAppointmentKafkaDto> activeAppointments(final List<CustomerAppointmentKafkaDto> appointments) {
        return appointments.stream()
                .filter(appointment -> !Boolean.TRUE.equals(appointment.getDeleted()))
                .toList();
    }

    private List<UserAppointmentKafkaDto> activeUserAppointments(final List<UserAppointmentKafkaDto> appointments) {
        return appointments.stream()
                .filter(appointment -> !Boolean.TRUE.equals(appointment.getDeleted()))
                .toList();
    }

    private boolean belongsToUser(final CustomerAppointmentKafkaDto appointment, final String username) {
        return Objects.equals(appointment.getUserId(), username);
    }

    private boolean belongsToUser(final UserAppointmentKafkaDto appointment, final String username) {
        return Objects.equals(appointment.getUserId(), username);
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
        final Instant endsAt = appointment.getEnd() == null
                ? startsAt.plus(CalendarEventFactory.DEFAULT_APPOINTMENT_DURATION)
                : toInstant(appointment.getEnd());
        return timeRange.overlaps(startsAt, endsAt);
    }

    private boolean matchesTimeRange(final UserAppointmentKafkaDto appointment, final DavTimeRange timeRange) {
        if (timeRange == null || appointment.getStart() == null) {
            return true;
        }
        final Instant startsAt = toInstant(appointment.getStart());
        final Instant endsAt = appointment.getEnd() == null
                ? startsAt.plus(CalendarEventFactory.DEFAULT_APPOINTMENT_DURATION)
                : toInstant(appointment.getEnd());
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
                List.of(new CalendarDataType(DavMediaTypes.ICALENDAR, DavMediaTypes.ICALENDAR_VERSION)));
    }

    private String customerAppointmentSyncToken(final List<CustomerAppointmentKafkaDto> appointments) {
        final int token = appointments.stream()
                .map(appointment -> Objects.hash(
                        appointment.getId(),
                        appointment.getUpdated(),
                        appointment.getEnd(),
                        appointment.getDeleted(),
                        customer(appointment).map(CustomerKafkaDto::getUpdated).orElse(null),
                        customer(appointment).map(CustomerKafkaDto::getDeleted).orElse(null)))
                .sorted()
                .reduce(1, (left, right) -> 31 * left + right);
        return CUSTOMER_APPOINTMENT_SYNC_STATE_PREFIX + Integer.toUnsignedString(token);
    }

    private String userAppointmentSyncToken(final List<UserAppointmentKafkaDto> appointments) {
        final int token = appointments.stream()
                .map(appointment ->
                        Objects.hash(appointment.getId(), appointment.getUpdated(), appointment.getDeleted()))
                .sorted()
                .reduce(1, (left, right) -> 31 * left + right);
        return PRIVATE_APPOINTMENT_SYNC_STATE_PREFIX + Integer.toUnsignedString(token);
    }

    private String customerAppointmentEtag(
            final CustomerAppointmentKafkaDto appointment, final java.util.Optional<CustomerKafkaDto> customer) {
        return DavResourceMetadata.etag(
                appointment.getId(),
                appointment.getUpdated(),
                appointment.getEnd(),
                customer.map(CustomerKafkaDto::getUpdated).orElse(null),
                customer.map(CustomerKafkaDto::getDeleted).orElse(null));
    }

    private LocalDateTime customerAppointmentLastModified(
            final CustomerAppointmentKafkaDto appointment, final java.util.Optional<CustomerKafkaDto> customer) {
        return customer.map(CustomerKafkaDto::getUpdated)
                .filter(customerUpdated ->
                        appointment.getUpdated() == null || customerUpdated.isAfter(appointment.getUpdated()))
                .orElse(appointment.getUpdated());
    }

    private String etag(final UserAppointmentKafkaDto appointment) {
        return DavResourceMetadata.etag(appointment.getId(), appointment.getUpdated());
    }

    private String principalHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.PRINCIPALS_PATH + "/" + username + "/";
    }

    private String calendarHomeHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.CALENDAR_PATH + "/" + username + "/";
    }

    private String appointmentCalendarHref(final String hrefBasePath, final String username) {
        return calendarHomeHref(hrefBasePath, username) + APIConstants.APPOINTMENTS_SEGMENT + "/";
    }

    private String privateCalendarHref(final String hrefBasePath, final String username) {
        return calendarHomeHref(hrefBasePath, username) + APIConstants.PRIVATE_SEGMENT + "/";
    }

    private String appointmentHref(final String hrefBasePath, final String username, final long appointmentId) {
        return appointmentCalendarHref(hrefBasePath, username) + appointmentId + ".ics";
    }

    private String privateAppointmentHref(final String hrefBasePath, final String username, final long appointmentId) {
        return privateCalendarHref(hrefBasePath, username) + appointmentId + ".ics";
    }
}
