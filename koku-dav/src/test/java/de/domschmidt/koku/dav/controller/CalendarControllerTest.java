package de.domschmidt.koku.dav.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dav.http.DavHttpHeaders;
import de.domschmidt.koku.dav.model.DavMultiStatus;
import de.domschmidt.koku.dav.model.DavRequest;
import de.domschmidt.koku.dav.service.CalDavService;
import de.domschmidt.koku.dav.xml.DavXmlReader;
import de.domschmidt.koku.dav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

class CalendarControllerTest {

    private final DavXmlReader reader = mock(DavXmlReader.class);
    private final DavXmlWriter writer = mock(DavXmlWriter.class);
    private final CalDavService service = mock(CalDavService.class);
    private final Authentication authentication = mock(Authentication.class);
    private CalendarController controller;

    @BeforeEach
    void setUp() {
        controller = new CalendarController(reader, writer, service);
        when(authentication.getName()).thenReturn("user-id");
    }

    @Test
    void returnsCustomerAppointmentCalendarWithCachingDisabled() {
        when(service.getAppointmentEtag("user-id", 42L)).thenReturn("etag-42");
        when(service.getICalendar("user-id", 42L)).thenReturn("customer-calendar");

        final ResponseEntity<String> response = controller.getAppointment("ignored", 42L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("customer-calendar");
        assertThat(response.getHeaders().getETag()).isEqualTo("etag-42");
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        assertThat(response.getHeaders().getContentType().toString())
                .contains("text/calendar")
                .contains("UTF-8");
    }

    @Test
    void returnsPrivateAppointmentCalendarWithCachingDisabled() {
        when(service.getPrivateAppointmentEtag("user-id", 7L)).thenReturn("etag-7");
        when(service.getPrivateICalendar("user-id", 7L)).thenReturn("private-calendar");

        final ResponseEntity<String> response = controller.getPrivateAppointment("ignored", 7L, authentication);

        assertThat(response.getBody()).isEqualTo("private-calendar");
        assertThat(response.getHeaders().getETag()).isEqualTo("etag-7");
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
    }

    @Test
    void delegatesPropfindAndBuildsDavMultistatusResponse() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final DavRequest davRequest = mock(DavRequest.class);
        final DavMultiStatus multiStatus = mock(DavMultiStatus.class);
        when(request.getMethod()).thenReturn("PROPFIND");
        when(reader.read(request)).thenReturn(davRequest);
        when(service.handleCalendarHome(davRequest, "user-id")).thenReturn(multiStatus);
        when(writer.write(multiStatus)).thenReturn("<multistatus/>");

        final ResponseEntity<String> response = controller.calendarHomeRequest(request, "ignored", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(response.getBody()).isEqualTo("<multistatus/>");
        assertThat(response.getHeaders().getFirst(DavHttpHeaders.DAV)).isEqualTo(DavHttpHeaders.DAV_COMPLIANCE);
        assertThat(response.getHeaders().getFirst(HttpHeaders.VARY)).isEqualTo(HttpHeaders.AUTHORIZATION);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        verify(service).handleCalendarHome(davRequest, "user-id");
    }

    @Test
    void delegatesReportForCustomerAppointmentCalendar() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final DavRequest davRequest = mock(DavRequest.class);
        final DavMultiStatus multiStatus = mock(DavMultiStatus.class);
        when(request.getMethod()).thenReturn("REPORT");
        when(reader.read(request)).thenReturn(davRequest);
        when(service.handleAppointmentCalendar(davRequest, "user-id")).thenReturn(multiStatus);
        when(writer.write(multiStatus)).thenReturn("<appointments/>");

        final ResponseEntity<String> response =
                controller.appointmentCalendarRequest(request, "ignored", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(response.getBody()).isEqualTo("<appointments/>");
        verify(service).handleAppointmentCalendar(davRequest, "user-id");
    }

    @Test
    void delegatesPropfindForPrivateAppointmentCalendar() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final DavRequest davRequest = mock(DavRequest.class);
        final DavMultiStatus multiStatus = mock(DavMultiStatus.class);
        when(request.getMethod()).thenReturn("PROPFIND");
        when(reader.read(request)).thenReturn(davRequest);
        when(service.handlePrivateCalendar(davRequest, "user-id")).thenReturn(multiStatus);
        when(writer.write(multiStatus)).thenReturn("<private/>");

        final ResponseEntity<String> response = controller.privateCalendarRequest(request, "ignored", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(response.getBody()).isEqualTo("<private/>");
        verify(service).handlePrivateCalendar(davRequest, "user-id");
    }

    @Test
    void rejectsUnsupportedCalendarHomeMethodBeforeParsingBody() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("REPORT");

        assertThatThrownBy(() -> controller.calendarHomeRequest(request, "ignored", authentication))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }
}
