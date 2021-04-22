package de.domschmidt.koku.controller.customer;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.customer.CustomerAppointmentDto;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CustomerAppointmentsController extends AbstractController<CustomerAppointment, CustomerAppointmentDto, CustomerAppointmentSearchOptions> {

    private static final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer = new CustomerAppointmentToCustomerAppointmentDtoTransformer();
    private final ICustomerService customerService;

    @Autowired
    public CustomerAppointmentsController(
            final ICustomerAppointmentService customerAppointmentService,
            final ICustomerService customerService
    ) {
        super(customerAppointmentService, transformer);
        this.customerService = customerService;
    }

    @GetMapping(value = "/customers/{customerId}/appointments")
    @Transactional(readOnly = true)
    public List<CustomerAppointmentDto> findAll(@PathVariable("customerId") Long customerId) {
        final Customer customer = this.customerService.findById(customerId);
        final List<CustomerAppointment> customerAppointments = customer.getCustomerAppointments();
        return new CustomerAppointmentToCustomerAppointmentDtoTransformer().transformToDtoList(customerAppointments, false);
    }

    @GetMapping(value = "/customers/appointments/{id}")
    @Transactional(readOnly = true)
    public CustomerAppointmentDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PutMapping(value = "/customers/appointments/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("id") Long id, @RequestBody CustomerAppointmentDto updatedDto) {
        super.update(id, updatedDto);
    }

    @DeleteMapping(value = "/customers/appointments/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        super.delete(id);
    }

    @PostMapping(value = "/customers/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CustomerAppointmentDto create(@RequestBody CustomerAppointmentDto newDto) {
        return super.create(newDto);
    }


}
