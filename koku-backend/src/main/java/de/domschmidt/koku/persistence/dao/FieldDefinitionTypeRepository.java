package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.dynamic_documents.FieldDefinitionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldDefinitionTypeRepository extends JpaRepository<FieldDefinitionType, Long> {

}
