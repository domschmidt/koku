package de.domschmidt.koku.controller.appointment;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.CalendarLoadSettingsDto;
import de.domschmidt.koku.dto.ICalendarContent;
import de.domschmidt.koku.dto.customer.CustomerAppointmentDto;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.IPrivateAppointmentService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import de.domschmidt.koku.transformer.CustomerToCustomerBirthdayDtoTransformer;
import de.domschmidt.koku.transformer.PrivateAppointmentToPrivateAppointmentDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/appointments")
public class AppointmentController extends AbstractController<CustomerAppointment, CustomerAppointmentDto, CustomerAppointmentSearchOptions> {

    private static final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer = new CustomerAppointmentToCustomerAppointmentDtoTransformer();
    private static final CustomerToCustomerBirthdayDtoTransformer customerToCustomerBirthdayDtoTransformer = new CustomerToCustomerBirthdayDtoTransformer();
    private static final PrivateAppointmentToPrivateAppointmentDtoTransformer privateAppointmentToPrivateAppointmentDtoTransformer = new PrivateAppointmentToPrivateAppointmentDtoTransformer();
    private final ICustomerAppointmentService customerAppointmentService;
    private final ICustomerService customerService;
    private final IPrivateAppointmentService privateAppointmentService;

    @Autowired
    public AppointmentController(final ICustomerAppointmentService customerAppointmentService,
                                 final IPrivateAppointmentService privateAppointmentService,
                                 final ICustomerService customerService) {
        super(customerAppointmentService, transformer);
        this.customerAppointmentService = customerAppointmentService;
        this.privateAppointmentService = privateAppointmentService;
        this.customerService = customerService;
    }

    @PostMapping
    @Transactional(readOnly = true)
    public List<ICalendarContent> findAllCalendarContents(
            @RequestBody final CalendarLoadSettingsDto loadSettings,
            @PathVariable("userId") String userId
    ) {
        final List<ICalendarContent> result;

        final CustomerAppointmentSearchOptions customerAppointmentSearchOptions = CustomerAppointmentSearchOptions.builder()
                .start(loadSettings.getStart())
                .end(loadSettings.getEnd())
                .userId("@self".equalsIgnoreCase(userId) ? null : Long.parseLong(userId))
                .build();

        if (customerAppointmentSearchOptions.getStart() != null || customerAppointmentSearchOptions.getEnd() != null) {
            result = new ArrayList<>();
            if (loadSettings.getLoadCustomerAppointments()) {
                result.addAll(transformer.transformToDtoList(this.customerAppointmentService.findAllAppointments(customerAppointmentSearchOptions)));
            }
            if (loadSettings.getLoadCustomerBirthdays()) {
                result.addAll(customerToCustomerBirthdayDtoTransformer.transformToDtoList(this.customerService.findAllCustomersWithBirthdayBetween(
                        customerAppointmentSearchOptions.getStart(),
                        customerAppointmentSearchOptions.getEnd()
                )));
            }
            if (loadSettings.getLoadPrivateAppointments() && ("@self".equalsIgnoreCase(userId))) {
                result.addAll(privateAppointmentToPrivateAppointmentDtoTransformer.transformToDtoList(this.privateAppointmentService.findAllMyNextAppointments(
                        customerAppointmentSearchOptions.getStart().atStartOfDay(),
                        customerAppointmentSearchOptions.getEnd().atTime(23, 59, 59, 999999999)
                )));
            }
        } else {
            result = new ArrayList<>(super.findAll(customerAppointmentSearchOptions));
        }
        return result;
    }

}
