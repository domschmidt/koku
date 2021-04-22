package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ActivityStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityStepRepository extends JpaRepository<ActivityStep, Long> {

    List<ActivityStep> findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseOrderByDescriptionAsc(String description);

}
