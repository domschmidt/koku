package de.domschmidt.koku.user.controller;

import de.domschmidt.dashboard.dto.DashboardViewDto;
import de.domschmidt.dashboard.factory.DashboardViewFactory;
import de.domschmidt.dashboard.factory.DefaultDashboardViewContentIdGenerator;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentDto;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import de.domschmidt.koku.dto.dashboard.containers.grid.DashboardGridContainerDto;
import de.domschmidt.koku.dto.dashboard.panels.calendar.DashboardAppointmentsPanelDto;
import de.domschmidt.koku.dto.dashboard.panels.calendar.DashboardAppointmentsPanelListSourceDto;
import de.domschmidt.koku.dto.dashboard.panels.text.DashboardTextPanelDto;
import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
import de.domschmidt.listquery.dto.request.EnumSearchOperatorHint;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/welcome")
@Slf4j
@RequiredArgsConstructor
public class UserWelcomeController {

    @GetMapping()
    public DashboardViewDto getDashboardView(@AuthenticationPrincipal Jwt jwt) {
        final DashboardViewFactory dashboardFactory = new DashboardViewFactory(
                new DefaultDashboardViewContentIdGenerator(),
                DashboardGridContainerDto.builder().cols(1).maxWidthInPx(800).build());

        dashboardFactory.addPanel(DashboardTextPanelDto.builder()
                .headline(String.format("Willkommen zurück, %s! ✨", jwt.getClaimAsString("name")))
                .color(KokuColorEnum.PINK)
                .build());

        final List<DashboardAppointmentsPanelListSourceDto> appointmentListSources = List.of(
                DashboardAppointmentsPanelListSourceDto.builder()
                        .sourceUrl("/services/customers/customers/query")
                        .idPath(KokuCustomerDto.Fields.id)
                        .startDateFieldSelectionPath(KokuCustomerDto.Fields.birthday)
                        .endDateFieldSelectionPath(KokuCustomerDto.Fields.birthday)
                        .searchOperatorHint(EnumSearchOperatorHint.YEARLY_RECURRING)
                        .textFieldSelectionPath(KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis)
                        .sourceItemText("Geburtstag")
                        .sourceItemColor(KokuColorEnum.YELLOW)
                        .allDay(true)
                        .deletedFieldSelectionPath(KokuCustomerDto.Fields.deleted)
                        .build(),
                DashboardAppointmentsPanelListSourceDto.builder()
                        .sourceUrl("/services/customers/customers/appointments/query")
                        .idPath(KokuCustomerAppointmentDto.Fields.id)
                        .startDateFieldSelectionPath(KokuCustomerAppointmentDto.Fields.date)
                        .endDateFieldSelectionPath(KokuCustomerAppointmentDto.Fields.date)
                        .startTimeFieldSelectionPath(KokuCustomerAppointmentDto.Fields.time)
                        .endTimeFieldSelectionPath(KokuCustomerAppointmentDto.Fields.time)
                        .sourceItemText("Kundentermin")
                        .sourceItemColor(KokuColorEnum.BLUE)
                        .textFieldSelectionPath(KokuCustomerAppointmentDto.Fields.customerName)
                        .notesTextFieldSelectionPath(KokuCustomerAppointmentDto.Fields.additionalInfo)
                        .userIdFieldSelectionPath(KokuCustomerAppointmentDto.Fields.userId)
                        .deletedFieldSelectionPath(KokuCustomerAppointmentDto.Fields.deleted)
                        .build(),
                DashboardAppointmentsPanelListSourceDto.builder()
                        .sourceUrl("/services/users/users/appointments/query")
                        .idPath(KokuUserAppointmentDto.Fields.id)
                        .startDateFieldSelectionPath(KokuUserAppointmentDto.Fields.startDate)
                        .endDateFieldSelectionPath(KokuUserAppointmentDto.Fields.endDate)
                        .startTimeFieldSelectionPath(KokuUserAppointmentDto.Fields.startTime)
                        .endTimeFieldSelectionPath(KokuUserAppointmentDto.Fields.endTime)
                        .textFieldSelectionPath(KokuUserAppointmentDto.Fields.description)
                        .userIdFieldSelectionPath(KokuUserAppointmentDto.Fields.userId)
                        .deletedFieldSelectionPath(KokuUserAppointmentDto.Fields.deleted)
                        .sourceItemText("Privater Termin")
                        .sourceItemColor(KokuColorEnum.GREEN)
                        .build());
        final LocalDate now = LocalDate.now();
        dashboardFactory.addPanel(DashboardAppointmentsPanelDto.builder()
                .headline("Heutige Termine")
                .emptyMessage("Keine Termine")
                .start(now.atTime(LocalTime.MIN))
                .end(now.atTime(LocalTime.MAX))
                .listSources(appointmentListSources)
                .build());
        dashboardFactory.addPanel(DashboardAppointmentsPanelDto.builder()
                .headline("Anstehende Termine für Morgen")
                .emptyMessage("Keine Termine")
                .start(now.plusDays(1).atTime(LocalTime.MIN))
                .end(now.plusDays(1).atTime(LocalTime.MAX))
                .listSources(appointmentListSources)
                .build());
        dashboardFactory.addPanel(DashboardAppointmentsPanelDto.builder()
                .headline("Anstehende Termine für Übermorgen")
                .emptyMessage("Keine Termine")
                .start(now.plusDays(2).atTime(LocalTime.MIN))
                .end(now.plusDays(2).atTime(LocalTime.MAX))
                .listSources(appointmentListSources)
                .build());

        return dashboardFactory.create();
    }
}
