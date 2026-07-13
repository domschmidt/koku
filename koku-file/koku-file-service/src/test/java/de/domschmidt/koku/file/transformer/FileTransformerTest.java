package de.domschmidt.koku.file.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.file.persistence.File;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FileTransformerTest {
    @Test
    void mapsMetadataWithoutLoadingContentIntoDto() {
        final UUID id = UUID.randomUUID();
        final File file = new File(id, "note.txt", 42L, "text/plain", new byte[] {1, 2}, 2L);
        file.setDeleted(true);

        final var dto = new FileToFileDtoTransformer().transformToDto(file);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getFilename()).isEqualTo("note.txt");
        assertThat(dto.getCustomerId()).isEqualTo(42L);
        assertThat(dto.getMimeType()).isEqualTo("text/plain");
        assertThat(dto.getSize()).isEqualTo(2L);
        assertThat(dto.getDeleted()).isTrue();
        assertThat(new File(null, "generated.txt", null, "text/plain", new byte[0], 0).getId())
                .isNotNull();
    }
}
