package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseOrderByDescriptionAsc(String description);

}
