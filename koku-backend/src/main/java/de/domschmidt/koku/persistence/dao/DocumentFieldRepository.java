package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.dynamic_documents.DocumentField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentFieldRepository extends JpaRepository<DocumentField, Long> {

}
