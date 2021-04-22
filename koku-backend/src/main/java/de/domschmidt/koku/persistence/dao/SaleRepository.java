package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

}
