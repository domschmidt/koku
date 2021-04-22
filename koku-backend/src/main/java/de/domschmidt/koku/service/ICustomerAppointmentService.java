package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;

import java.util.List;

public interface ICustomerAppointmentService extends IOperations<CustomerAppointment, CustomerAppointmentSearchOptions> {

    List<CustomerAppointment> findAllAppointments(CustomerAppointmentSearchOptions searchOptions);
    List<CustomerAppointment> findAllAppointmentsOfAllUsers(CustomerAppointmentSearchOptions searchOptions);

}