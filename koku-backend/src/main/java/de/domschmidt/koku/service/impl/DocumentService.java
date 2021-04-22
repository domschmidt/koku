package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.DocumentRepository;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.service.IDocumentService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentService extends AbstractService<DynamicDocument, DocumentSearchOptions> implements IDocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(final DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<DynamicDocument, Long> getDao() {
        return this.documentRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<DynamicDocument> findAll(final DocumentSearchOptions documentSearchOptions) {
        return this.documentRepository.findAllByDeletedIsFalseAndDescriptionContainingIgnoreCaseOrderByDescriptionAsc(
                documentSearchOptions.getSearch()
        );
    }

}
