package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseOrderByDescriptionAsc(String description);

}
