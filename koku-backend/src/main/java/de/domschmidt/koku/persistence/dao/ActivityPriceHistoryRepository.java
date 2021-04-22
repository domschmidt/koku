package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ActivityPriceHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityPriceHistoryRepository extends JpaRepository<ActivityPriceHistoryEntry, Long> {

}
