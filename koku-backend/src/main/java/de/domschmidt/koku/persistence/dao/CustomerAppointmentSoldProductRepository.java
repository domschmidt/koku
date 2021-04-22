package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAppointmentSoldProductRepository extends JpaRepository<CustomerAppointmentSoldProduct, Long> {

}
