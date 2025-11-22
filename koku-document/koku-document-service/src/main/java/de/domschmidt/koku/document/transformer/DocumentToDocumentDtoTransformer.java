package de.domschmidt.koku.document.transformer;

import de.domschmidt.koku.document.persistence.Document;
import de.domschmidt.koku.dto.document.KokuDocumentDto;
import org.springframework.stereotype.Component;

@Component
public class DocumentToDocumentDtoTransformer {

    public KokuDocumentDto transformToDto(final Document model) {
        return KokuDocumentDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .template(model.getTemplate())
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

    public Document transformToEntity(
            final Document model,
            final KokuDocumentDto updatedDto
    ) {
        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }
        if (updatedDto.getTemplate() != null) {
            model.setTemplate(updatedDto.getTemplate());
        }

        return model;
    }
}
