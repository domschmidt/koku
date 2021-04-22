package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {


}
