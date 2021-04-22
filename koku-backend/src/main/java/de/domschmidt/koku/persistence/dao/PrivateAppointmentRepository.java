package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.PrivateAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PrivateAppointmentRepository extends JpaRepository<PrivateAppointment, Long> {

    List<PrivateAppointment> findAllByUser_UsernameEqualsIgnoreCaseAndStartIsGreaterThanEqualAndStartLessThanEqualAndDeletedIsFalse(String username, LocalDateTime start, LocalDateTime until);

}
