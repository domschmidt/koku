package de.domschmidt.koku.customer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAppointmentRepository extends JpaRepository<CustomerAppointment, Long> {

}
