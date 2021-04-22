package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ActivitySequenceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivitySequenceRepository extends JpaRepository<ActivitySequenceItem, Long> {

}
