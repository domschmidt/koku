package de.domschmidt.koku.file.transformer;

import de.domschmidt.koku.dto.file.KokuFileDto;
import de.domschmidt.koku.file.persistence.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileToFileDtoTransformer {

    public KokuFileDto transformToDto(final File model) {
        return KokuFileDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .filename(model.getFilename())
                .customerId(model.getCustomerId())
                .size(model.getSize())
                .mimeType(model.getMimeType())
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
