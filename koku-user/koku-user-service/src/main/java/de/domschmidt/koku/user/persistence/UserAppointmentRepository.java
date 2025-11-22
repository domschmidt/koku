package de.domschmidt.koku.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAppointmentRepository extends JpaRepository<UserAppointment, Long> {

}
