package de.domschmidt.koku.file.transformer;

import de.domschmidt.koku.dto.file.KokuFileDto;
import de.domschmidt.koku.dto.file.KokuFileRefDto;
import de.domschmidt.koku.file.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.file.persistence.File;
import de.domschmidt.koku.file.persistence.FileRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileToFileDtoTransformer {

    private final CustomerKTableProcessor customerKTableProcessor;

    public KokuFileRefDto transformRefToDto(final FileRef model) {
        KokuFileRefDto ref = null;
        if (model != null) {
            switch (model) {
                case CUSTOMER: {
                    ref = KokuFileRefDto.CUSTOMER;
                    break;
                }
            }
        }
        return ref;
    }

    public FileRef transformRefToEntity(final KokuFileRefDto model) {
        FileRef ref = null;
        if (model != null) {
            switch (model) {
                case CUSTOMER: {
                    ref = FileRef.CUSTOMER;
                    break;
                }
            }
        }
        return ref;
    }

    public KokuFileDto transformToDto(final File model) {
        String refName = null;
        if (model.getRef() != null) {
            switch (model.getRef()) {
                case CUSTOMER: {
                    refName = this.customerKTableProcessor.getCustomers().get(Long.valueOf(model.getRefId())).getFullname();
                    break;
                }
            }
        }

        return KokuFileDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .filename(model.getFilename())
                .ref(transformRefToDto(model.getRef()))
                .refId(model.getRefId())
                .refName(refName)
                .size(model.getSize())
                .mimeType(model.getMimeType())
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

}
