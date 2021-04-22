package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.service.searchoptions.CustomerSearchOptions;

import java.time.LocalDate;
import java.util.List;

public interface ICustomerService extends IOperations<Customer, CustomerSearchOptions> {

    List<Customer> findAllCustomersWithBirthdayBetween(LocalDate from, LocalDate until);

}