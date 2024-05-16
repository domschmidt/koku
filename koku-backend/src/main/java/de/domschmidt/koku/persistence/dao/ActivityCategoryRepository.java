package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {

    List<ActivityCategory> findAllByDescriptionContainingIgnoreCaseOrderByDescriptionAsc(String description);

}
