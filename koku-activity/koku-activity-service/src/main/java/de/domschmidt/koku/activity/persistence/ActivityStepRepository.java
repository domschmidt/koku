package de.domschmidt.koku.activity.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityStepRepository extends JpaRepository<ActivityStep, Long> {

}
