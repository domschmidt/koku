package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ProductPriceHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistoryEntry, Long> {

}
