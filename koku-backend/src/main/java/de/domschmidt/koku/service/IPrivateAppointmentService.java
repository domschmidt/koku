package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.PrivateAppointment;
import de.domschmidt.koku.service.searchoptions.PrivateAppointmentSearchOptions;

import java.time.LocalDateTime;
import java.util.List;

public interface IPrivateAppointmentService extends IOperations<PrivateAppointment, PrivateAppointmentSearchOptions> {

    List<PrivateAppointment> findAllMyNextAppointments(LocalDateTime from, LocalDateTime until);

}