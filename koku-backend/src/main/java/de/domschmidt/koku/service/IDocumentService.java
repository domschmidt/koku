package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;

public interface IDocumentService extends IOperations<DynamicDocument, DocumentSearchOptions> {

}