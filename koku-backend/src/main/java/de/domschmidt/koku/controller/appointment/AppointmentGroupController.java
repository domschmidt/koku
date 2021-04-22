package de.domschmidt.koku.controller.appointment;

import de.domschmidt.koku.dto.AppointmentGroupDto;
import de.domschmidt.koku.dto.customer.CustomerAppointmentDto;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/users/@self/appointmentgroups")
public class AppointmentGroupController {

    private final ICustomerAppointmentService customerAppointmentService;

    @Autowired
    public AppointmentGroupController(final ICustomerAppointmentService customerAppointmentService) {
        this.customerAppointmentService = customerAppointmentService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<AppointmentGroupDto> findAll() {
        final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer = new CustomerAppointmentToCustomerAppointmentDtoTransformer();
        final Map<LocalDate, List<CustomerAppointmentDto>> sortedAndGroupedAppointments = new HashMap<>();
        final List<AppointmentGroupDto> appointmentGroupList = new ArrayList<>();
        final CustomerAppointmentSearchOptions searchOptions = CustomerAppointmentSearchOptions.builder()
                .start(LocalDate.now())
                .end(LocalDate.now().plusDays(7))
                .build();
        final List<CustomerAppointment> allMyNextCustomerAppointments =
                this.customerAppointmentService.findAllAppointments(searchOptions);
        for (final CustomerAppointment customerAppointment : allMyNextCustomerAppointments) {
            if (!sortedAndGroupedAppointments.containsKey(customerAppointment.getStart().toLocalDate())) {
                sortedAndGroupedAppointments.put(customerAppointment.getStart().toLocalDate(), new ArrayList<>());
            }
            sortedAndGroupedAppointments.get(customerAppointment.getStart().toLocalDate()).add(transformer.transformToDto(customerAppointment));
        }
        for (final Map.Entry<LocalDate, List<CustomerAppointmentDto>> entry : sortedAndGroupedAppointments.entrySet()) {
            final List<CustomerAppointmentDto> appointmentsForThisDay = new ArrayList<>(entry.getValue());
            appointmentsForThisDay.sort(Comparator.comparing(CustomerAppointmentDto::getStartTime));
            appointmentGroupList.add(AppointmentGroupDto.builder()
                    .appointments(appointmentsForThisDay)
                    .date(entry.getKey())
                    .build());
        }
        appointmentGroupList.sort(Comparator.comparing(AppointmentGroupDto::getDate));
        return appointmentGroupList;
    }

}
