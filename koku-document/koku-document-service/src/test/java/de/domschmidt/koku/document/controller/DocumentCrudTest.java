package de.domschmidt.koku.document.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.document.persistence.Document;
import de.domschmidt.koku.document.persistence.DocumentRepository;
import de.domschmidt.koku.document.transformer.DocumentToDocumentDtoTransformer;
import de.domschmidt.koku.dto.document.KokuDocumentDto;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class DocumentCrudTest {
    private final DocumentRepository repository = mock(DocumentRepository.class);
    private final DocumentToDocumentDtoTransformer transformer = mock(DocumentToDocumentDtoTransformer.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private DocumentController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentController(repository, transformer, entityManager);
    }

    @Test
    void readAndMissingPathsAreDefined() {
        final Document document = document(false, 2L);
        when(repository.findById(5L)).thenReturn(Optional.of(document));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(document))
                .thenReturn(KokuDocumentDto.builder().id(5L).build());
        assertThat(controller.read(5L).getId()).isEqualTo(5L);
        assertThatThrownBy(() -> controller.read(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void staleUpdateRequiresForce() {
        final Document document = document(false, 3L);
        final KokuDocumentDto update = KokuDocumentDto.builder().version(2L).build();
        when(entityManager.getReference(Document.class, 5L)).thenReturn(document);
        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(document, update);
        verify(entityManager).flush();
    }

    @Test
    void lifecycleAndCreateContractsAreDefined() {
        final Document document = document(false, 0L);
        when(entityManager.getReference(Document.class, 5L)).thenReturn(document);
        controller.delete(5L);
        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        controller.restore(5L);
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);

        final KokuDocumentDto input = KokuDocumentDto.builder().name("Template").build();
        when(transformer.transformToEntity(any(Document.class), org.mockito.Mockito.same(input)))
                .thenReturn(document);
        when(repository.saveAndFlush(document)).thenReturn(document);
        controller.create(input);
        verify(transformer, org.mockito.Mockito.atLeastOnce()).transformToDto(document);
    }

    private static Document document(boolean deleted, Long version) {
        final Document document = new Document();
        document.setId(5L);
        document.setName("Template");
        document.setVersion(version);
        document.setDeleted(deleted);
        return document;
    }
}
