package de.domschmidt.koku.carddav.controller;

import de.domschmidt.koku.carddav.APIConstants;
import de.domschmidt.koku.carddav.model.DavMethod;
import de.domschmidt.koku.carddav.service.CalDavService;
import de.domschmidt.koku.carddav.xml.DavXmlReader;
import de.domschmidt.koku.carddav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(
            value = {
                "/{userName}",
                "/{userName}/",
            })
    public ResponseEntity<String> calendarHomeRequest(
            final HttpServletRequest request, final @PathVariable String userName) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND),
                davRequest -> calDavService.handleCalendarHome(davRequest, userName));
    }

    @RequestMapping(
            value = {
                "/{userName}/appointments",
                "/{userName}/appointments/",
            })
    public ResponseEntity<String> appointmentCalendarRequest(
            final HttpServletRequest request, final @PathVariable String userName) {
        return multistatus(
                request,
                Set.of(DavMethod.PROPFIND, DavMethod.REPORT),
                davRequest -> calDavService.handleAppointmentCalendar(davRequest, userName));
    }

    @GetMapping(
            value = {
                "/{userName}/appointments/{appointmentId}.ics",
            },
            produces = "text/calendar")
    public ResponseEntity<String> getAppointment(
            final @PathVariable String userName, final @PathVariable long appointmentId) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.ETAG, calDavService.getAppointmentEtag(appointmentId))
                .body(calDavService.getICalendar(appointmentId));
    }
}
