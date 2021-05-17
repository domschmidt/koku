package de.domschmidt.koku.controller.user;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.user.PrivateAppointmentDto;
import de.domschmidt.koku.persistence.model.PrivateAppointment;
import de.domschmidt.koku.service.IPrivateAppointmentService;
import de.domschmidt.koku.service.searchoptions.PrivateAppointmentSearchOptions;
import de.domschmidt.koku.transformer.PrivateAppointmentToPrivateAppointmentDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class PrivateAppointmentsController extends AbstractController<PrivateAppointment, PrivateAppointmentDto, PrivateAppointmentSearchOptions>  {

    private static final PrivateAppointmentToPrivateAppointmentDtoTransformer transformer = new PrivateAppointmentToPrivateAppointmentDtoTransformer();

    @Autowired
    public PrivateAppointmentsController(
            final IPrivateAppointmentService privateAppointmentService
    ) {
        super(privateAppointmentService, transformer);
    }

    @GetMapping(value = "/users/@self/privateappointments/{id}")
    public PrivateAppointmentDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PutMapping(value = "/users/@self/privateappointments/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody PrivateAppointmentDto updatedDto) {
        super.update(id, updatedDto);
    }

    @PutMapping(value = "/users/@self/privateappointments/{id}/timing")
    @ResponseStatus(HttpStatus.OK)
    public void updateTiming(@PathVariable("id") Long id, @RequestBody PrivateAppointmentDto updatedDto) {
        final PrivateAppointmentDto existingAppointment = super.findByIdTransformed(id);
        existingAppointment.setStartDate(updatedDto.getStartDate());
        existingAppointment.setStartTime(updatedDto.getStartTime());
        existingAppointment.setEndDate(updatedDto.getEndDate());
        existingAppointment.setEndTime(updatedDto.getEndTime());
        super.update(id, existingAppointment);
    }

    @DeleteMapping(value = "/users/@self/privateappointments/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        this.service.deleteById(id);
    }

    @PostMapping(value = "/users/@self/privateappointments")
    @ResponseStatus(HttpStatus.CREATED)
    public PrivateAppointmentDto create(@RequestBody PrivateAppointmentDto newDto) {
        return super.create(newDto);
    }


}
