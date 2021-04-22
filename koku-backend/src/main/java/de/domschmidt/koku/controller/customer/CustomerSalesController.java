package de.domschmidt.koku.controller.customer;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.customer.CustomerSalesDto;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import de.domschmidt.koku.transformer.CustomerAppointmentToCustomerSalesDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerSalesController extends AbstractController<CustomerAppointment, CustomerSalesDto, CustomerAppointmentSearchOptions> {

    private static final CustomerAppointmentToCustomerSalesDtoTransformer transformer = new CustomerAppointmentToCustomerSalesDtoTransformer();
    private final ICustomerService customerService;

    @Autowired
    public CustomerSalesController(
            final ICustomerAppointmentService customerAppointmentService,
            final ICustomerService customerService
    ) {
        super(customerAppointmentService, transformer);
        this.customerService = customerService;
    }

    @GetMapping(value = "/customers/{customerId}/sales")
    @Transactional(readOnly = true)
    public List<CustomerSalesDto> findAll(@PathVariable("customerId") Long customerId) {
        final Customer customer = this.customerService.findById(customerId);
        final List<CustomerAppointment> customerAppointments = customer.getCustomerAppointments();
        return transformer.transformToDtoList(customerAppointments);
    }


}
