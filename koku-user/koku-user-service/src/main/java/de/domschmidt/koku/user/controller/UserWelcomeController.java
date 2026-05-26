package de.domschmidt.koku.user.controller;

import de.domschmidt.koku.contracts.dto.KokuColor;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardAppointmentsPanel;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardAppointmentsPanelListSource;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardGridContainer;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardTextPanel;
import de.domschmidt.koku.dashboard.contract.dto.KokuDashboardView;
import de.domschmidt.koku.dashboard.factory.DefaultDashboardViewContentIdGenerator;
import de.domschmidt.koku.dashboard.factory.KokuDashboardViewFactory;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentDto;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
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
    public KokuDashboardView getDashboardView(@AuthenticationPrincipal Jwt jwt) {
        final KokuDashboardViewFactory dashboardFactory = new KokuDashboardViewFactory(
                new DefaultDashboardViewContentIdGenerator(),
                new KokuDashboardGridContainer().cols(1).maxWidthInPx(800));

        dashboardFactory.addPanel(new KokuDashboardTextPanel()
                .headline(String.format("Willkommen zurück, %s! ✨", jwt.getClaimAsString("name")))
                .color(KokuColor.PINK));

        final List<KokuDashboardAppointmentsPanelListSource> appointmentListSources = List.of(
                new KokuDashboardAppointmentsPanelListSource()
                        .sourceUrl("/services/customers/customers/query")
                        .idPath(KokuCustomerDto.Fields.id)
                        .startDateFieldSelectionPath(KokuCustomerDto.Fields.birthday)
                        .endDateFieldSelectionPath(KokuCustomerDto.Fields.birthday)
                        .searchOperatorHint(
                                KokuDashboardAppointmentsPanelListSource.SearchOperatorHintEnum.YEARLY_RECURRING)
                        .textFieldSelectionPath(KokuCustomerDto.Fields.fullNameWithOnFirstNameBasis)
                        .sourceItemText("Geburtstag")
                        .sourceItemColor(KokuColor.YELLOW)
                        .allDay(true)
                        .deletedFieldSelectionPath(KokuCustomerDto.Fields.deleted),
                new KokuDashboardAppointmentsPanelListSource()
                        .sourceUrl("/services/customers/customers/appointments/query")
                        .idPath(KokuCustomerAppointmentDto.Fields.id)
                        .startDateFieldSelectionPath(KokuCustomerAppointmentDto.Fields.date)
                        .endDateFieldSelectionPath(KokuCustomerAppointmentDto.Fields.date)
                        .startTimeFieldSelectionPath(KokuCustomerAppointmentDto.Fields.time)
                        .endTimeFieldSelectionPath(KokuCustomerAppointmentDto.Fields.time)
                        .sourceItemText("Kundentermin")
                        .sourceItemColor(KokuColor.BLUE)
                        .textFieldSelectionPath(KokuCustomerAppointmentDto.Fields.customerName)
                        .notesTextFieldSelectionPath(KokuCustomerAppointmentDto.Fields.additionalInfo)
                        .userIdFieldSelectionPath(KokuCustomerAppointmentDto.Fields.userId)
                        .deletedFieldSelectionPath(KokuCustomerAppointmentDto.Fields.deleted),
                new KokuDashboardAppointmentsPanelListSource()
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
                        .sourceItemColor(KokuColor.GREEN));
        final LocalDate now = LocalDate.now();
        dashboardFactory.addPanel(new KokuDashboardAppointmentsPanel()
                .headline("Heutige Termine")
                .emptyMessage("Keine Termine")
                .start(now.atTime(LocalTime.MIN))
                .end(now.atTime(LocalTime.MAX))
                .listSources(appointmentListSources));
        dashboardFactory.addPanel(new KokuDashboardAppointmentsPanel()
                .headline("Anstehende Termine für Morgen")
                .emptyMessage("Keine Termine")
                .start(now.plusDays(1).atTime(LocalTime.MIN))
                .end(now.plusDays(1).atTime(LocalTime.MAX))
                .listSources(appointmentListSources));
        dashboardFactory.addPanel(new KokuDashboardAppointmentsPanel()
                .headline("Anstehende Termine für Übermorgen")
                .emptyMessage("Keine Termine")
                .start(now.plusDays(2).atTime(LocalTime.MIN))
                .end(now.plusDays(2).atTime(LocalTime.MAX))
                .listSources(appointmentListSources));

        return dashboardFactory.create();
    }
}
