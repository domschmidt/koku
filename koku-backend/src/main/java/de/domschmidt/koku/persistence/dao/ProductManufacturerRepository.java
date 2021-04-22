package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ProductManufacturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductManufacturerRepository extends JpaRepository<ProductManufacturer, Long> {

    List<ProductManufacturer> findAllByNameContainingIgnoreCaseAndDeletedIsFalseOrderByNameAsc(String description);

}
