package de.domschmidt.koku.file.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dto.file.KokuFileDto;
import de.domschmidt.koku.file.persistence.File;
import de.domschmidt.koku.file.persistence.FileRepository;
import de.domschmidt.koku.file.transformer.FileToFileDtoTransformer;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class FileCrudTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final FileRepository repository = mock(FileRepository.class);
    private final FileToFileDtoTransformer transformer = mock(FileToFileDtoTransformer.class);
    private FileController controller;

    @BeforeEach
    void setUp() {
        controller = new FileController(entityManager, repository, transformer, null);
    }

    @Test
    void metadataAndContentReadsUseSamePersistedFile() throws Exception {
        final UUID id = UUID.randomUUID();
        final File file = file(id, false);
        when(repository.getReferenceById(id)).thenReturn(file);
        when(transformer.transformToDto(file))
                .thenReturn(KokuFileDto.builder().id(id).build());
        assertThat(controller.read(id).getId()).isEqualTo(id);
        assertThat(controller.readFile(id).getHeaders().getContentType()).hasToString("text/plain");
        assertThat(controller.readFile(id).getBody().getContentAsByteArray())
                .isEqualTo("content".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void lifecycleTogglesAndRejectsRepeatedOperations() {
        final UUID id = UUID.randomUUID();
        final File file = file(id, false);
        when(entityManager.getReference(File.class, id)).thenReturn(file);
        controller.delete(id);
        assertThatThrownBy(() -> controller.delete(id)).isInstanceOf(ResponseStatusException.class);
        controller.restore(id);
        assertThatThrownBy(() -> controller.restore(id)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void uploadPersistsAllMultipartMetadata() throws Exception {
        final UUID id = UUID.randomUUID();
        final MockMultipartFile upload =
                new MockMultipartFile("file", "note.txt", "text/plain", "content".getBytes(StandardCharsets.UTF_8));
        when(repository.saveAndFlush(org.mockito.ArgumentMatchers.any(File.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        controller.create(upload, id, 42L);

        verify(repository)
                .saveAndFlush(org.mockito.ArgumentMatchers.argThat(
                        file -> file.getId().equals(id)
                                && file.getCustomerId().equals(42L)
                                && file.getFilename().equals("note.txt")
                                && file.getSize() == 7L));
    }

    private static File file(UUID id, boolean deleted) {
        final File file = new File(id, "note.txt", 42L, "text/plain", "content".getBytes(StandardCharsets.UTF_8), 7L);
        file.setDeleted(deleted);
        return file;
    }
}
