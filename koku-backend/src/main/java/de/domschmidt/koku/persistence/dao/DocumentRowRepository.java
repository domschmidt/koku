package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.dynamic_documents.DocumentRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRowRepository extends JpaRepository<DocumentRow, Long> {

}
