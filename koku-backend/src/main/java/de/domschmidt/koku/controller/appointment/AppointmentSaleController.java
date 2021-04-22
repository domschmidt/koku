package de.domschmidt.koku.controller.appointment;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.SaleDto;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.Sale;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.ISaleService;
import de.domschmidt.koku.service.searchoptions.SaleSearchOptions;
import de.domschmidt.koku.transformer.SaleToSaleDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/appointments/{appointmentId}/sales")
public class AppointmentSaleController extends AbstractController<Sale, SaleDto, SaleSearchOptions> {

    private final ICustomerAppointmentService customerAppointmentService;

    @Autowired
    public AppointmentSaleController(final ISaleService saleService,
                                     final ICustomerAppointmentService customerAppointmentService) {
        super(saleService, new SaleToSaleDtoTransformer());
        this.customerAppointmentService = customerAppointmentService;
    }

    @GetMapping
    public List<SaleDto> findAll(@PathVariable("appointmentId") Long appointmentId) {
        final CustomerAppointment customerAppointment = customerAppointmentService.findById(appointmentId);
        final List<Sale> sales = customerAppointment.getSales();
        return new SaleToSaleDtoTransformer().transformToDtoList(sales);
    }

    @PutMapping(value = "/{saleId}")
    @ResponseStatus(HttpStatus.OK)
    public void bind(@PathVariable("appointmentId") Long appointmentId, @PathVariable("saleId") Long saleId) {
        final Sale sale = super.findById(saleId);
        final CustomerAppointment customerAppointment = customerAppointmentService.findById(appointmentId);
        if (sale == null || customerAppointment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        customerAppointment.getSales().add(sale);
        customerAppointmentService.update(customerAppointment);
    }

    @DeleteMapping(value = "/{saleId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("appointmentId") Long appointmentId, @PathVariable("saleId") Long saleId) {
        final Sale sale = super.findById(saleId);
        final CustomerAppointment customerAppointment = customerAppointmentService.findById(appointmentId);
        if (sale == null || customerAppointment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        customerAppointment.getSales().remove(sale);
        customerAppointmentService.update(customerAppointment);
    }

}
