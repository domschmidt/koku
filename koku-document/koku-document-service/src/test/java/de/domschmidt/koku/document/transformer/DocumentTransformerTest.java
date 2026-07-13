package de.domschmidt.koku.document.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.document.persistence.Document;
import de.domschmidt.koku.dto.document.KokuDocumentDto;
import org.junit.jupiter.api.Test;

class DocumentTransformerTest {
    @Test
    void roundTripAndPartialUpdatePreserveDocumentContract() {
        final Document document = new Document();
        document.setId(5L);
        final DocumentToDocumentDtoTransformer transformer = new DocumentToDocumentDtoTransformer();
        transformer.transformToEntity(
                document,
                KokuDocumentDto.builder()
                        .name("Template")
                        .template("{json}")
                        .deleted(true)
                        .build());
        final KokuDocumentDto dto = transformer.transformToDto(document);
        assertThat(dto.getName()).isEqualTo("Template");
        assertThat(dto.getTemplate()).isEqualTo("{json}");
        assertThat(dto.getDeleted()).isTrue();

        transformer.transformToEntity(document, KokuDocumentDto.builder().build());
        assertThat(document.getName()).isEqualTo("Template");
    }
}
