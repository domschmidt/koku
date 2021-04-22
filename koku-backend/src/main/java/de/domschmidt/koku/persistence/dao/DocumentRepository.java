package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DynamicDocument, Long> {

    List<DynamicDocument> findAllByDeletedIsFalseAndDescriptionContainingIgnoreCaseOrderByDescriptionAsc(final String descriptionSearchStr);

}
