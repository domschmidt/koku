package de.domschmidt.koku.dav.controller;

import de.domschmidt.koku.dav.APIConstants;
import de.domschmidt.koku.dav.http.DavMediaTypes;
import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.service.CalDavService;
import de.domschmidt.koku.dav.xml.DavXmlReader;
import de.domschmidt.koku.dav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIConstants.CALENDAR_PATH)
public class CalendarController extends DavControllerSupport {

    private final CalDavService calDavService;

    public CalendarController(
            final DavXmlReader davXmlReader, final DavXmlWriter davXmlWriter, final CalDavService calDavService) {
        super(davXmlReader, davXmlWriter);
        this.calDavService = calDavService;
    }

    @DavRequestMapping(
            value = {
                "/{userName}",
                "/{userName}/",
            })
    public ResponseEntity<String> calendarHomeRequest(
            final HttpServletRequest request,
            final @PathVariable String userName,
            final Authentication authentication) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND),
                davRequest -> calDavService.handleCalendarHome(davRequest, authentication.getName()));
    }

    @DavRequestMapping(
            value = {
                "/{userName}/" + APIConstants.APPOINTMENTS_SEGMENT,
                "/{userName}/" + APIConstants.APPOINTMENTS_SEGMENT + "/",
            })
    public ResponseEntity<String> appointmentCalendarRequest(
            final HttpServletRequest request,
            final @PathVariable String userName,
            final Authentication authentication) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND, DavMethod.REPORT),
                davRequest -> calDavService.handleAppointmentCalendar(davRequest, authentication.getName()));
    }

    @DavRequestMapping(
            value = {
                "/{userName}/" + APIConstants.PRIVATE_SEGMENT,
                "/{userName}/" + APIConstants.PRIVATE_SEGMENT + "/",
            })
    public ResponseEntity<String> privateCalendarRequest(
            final HttpServletRequest request,
            final @PathVariable String userName,
            final Authentication authentication) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND, DavMethod.REPORT),
                davRequest -> calDavService.handlePrivateCalendar(davRequest, authentication.getName()));
    }

    @GetMapping(
            value = {
                "/{userName}/" + APIConstants.APPOINTMENTS_SEGMENT + "/{appointmentId}.ics",
            },
            produces = DavMediaTypes.ICALENDAR)
    public ResponseEntity<String> getAppointment(
            final @PathVariable String userName,
            final @PathVariable long appointmentId,
            final Authentication authentication) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(DavMediaTypes.ICALENDAR_UTF8))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.ETAG, calDavService.getAppointmentEtag(authentication.getName(), appointmentId))
                .body(calDavService.getICalendar(authentication.getName(), appointmentId));
    }

    @GetMapping(
            value = {
                "/{userName}/" + APIConstants.PRIVATE_SEGMENT + "/{appointmentId}.ics",
            },
            produces = DavMediaTypes.ICALENDAR)
    public ResponseEntity<String> getPrivateAppointment(
            final @PathVariable String userName,
            final @PathVariable long appointmentId,
            final Authentication authentication) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(DavMediaTypes.ICALENDAR_UTF8))
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.ETAG,
                        calDavService.getPrivateAppointmentEtag(authentication.getName(), appointmentId))
                .body(calDavService.getPrivateICalendar(authentication.getName(), appointmentId));
    }
}
