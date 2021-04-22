package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerAppointmentRepository extends JpaRepository<CustomerAppointment, Long> {

    @Transactional(readOnly = true)
    List<CustomerAppointment> findAllByUserEqualsAndStartIsGreaterThanEqualAndStartLessThanEqual(KokuUser user, LocalDateTime start, LocalDateTime until);

    @Transactional(readOnly = true)
    List<CustomerAppointment> findAllByStartIsGreaterThanEqualAndStartLessThanEqual(LocalDateTime start, LocalDateTime until);

    @Transactional(readOnly = true)
    List<CustomerAppointment> findAllByUserEquals(KokuUser user);

}
