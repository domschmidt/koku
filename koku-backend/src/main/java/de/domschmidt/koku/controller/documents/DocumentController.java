package de.domschmidt.koku.controller.documents;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.UploadDto;
import de.domschmidt.koku.dto.formular.DocumentContextDto;
import de.domschmidt.koku.dto.formular.DocumentContextEnumDto;
import de.domschmidt.koku.dto.formular.FormularDto;
import de.domschmidt.koku.dto.formular.FormularReplacementTokenDto;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.dynamic_documents.DocumentContext;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.persistence.model.uploads.FileUploadTag;
import de.domschmidt.koku.service.impl.DocumentService;
import de.domschmidt.koku.service.impl.StorageService;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;
import de.domschmidt.koku.transformer.DynamicDocumentToFormularDtoTransformer;
import de.domschmidt.koku.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/documents")
public class DocumentController extends AbstractController<DynamicDocument, FormularDto, DocumentSearchOptions> {


    private final DocumentPdfService documentPdfService;
    private final StorageService storageService;
    private final FileUploadRepository fileUploadRepository;
    private final DocumentService documentService;
    private final DynamicDocumentToFormularDtoTransformer dynamicDocumentToFormularDtoTransformer;

    @Autowired
    public DocumentController(
            final DocumentService documentService,
            final DynamicDocumentToFormularDtoTransformer dynamicDocumentToFormularDtoTransformer,
            final DocumentPdfService documentPdfService,
            final StorageService storageService,
            final FileUploadRepository fileUploadRepository
    ) {
        super(documentService, dynamicDocumentToFormularDtoTransformer);
        this.documentPdfService = documentPdfService;
        this.storageService = storageService;
        this.fileUploadRepository = fileUploadRepository;
        this.documentService = documentService;
        this.dynamicDocumentToFormularDtoTransformer = dynamicDocumentToFormularDtoTransformer;
    }

    @GetMapping()
    public List<FormularDto> findAll(final DocumentSearchOptions documentSearchOptions) {
        return super.findAll(documentSearchOptions);
    }

    @GetMapping(value = "/{id}")
    public FormularDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @GetMapping(value = "/contexts")
    public List<DocumentContextDto> getAllDocumentContexts() {
        final List<DocumentContextDto> result = new ArrayList<>();
        for (final DocumentContext currentContext : DocumentContext.values()) {
            result.add(new DynamicDocumentToFormularDtoTransformer().transformContext(currentContext));
        }
        return result;
    }

    @GetMapping(value = "/{id}/capture")
    public FormularDto getDocumentForCapture(@PathVariable("id") Long id) {
        final DynamicDocument document = this.documentService.findById(id);
        if (document == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (!document.getContext().equals(DocumentContext.NONE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return this.dynamicDocumentToFormularDtoTransformer.transformToDto(document, new HashMap<>());
    }

    @PostMapping(value = "/capture")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadDto createByOpenPdf(
            @RequestBody final FormularDto formularDto
    ) throws IOException {
        final ByteArrayOutputStream pdfByDynamicDocumentOutputStream =
                this.documentPdfService.createPdfByDynamicDocument(formularDto);

        final FileUpload newUpload = this.storageService.store(
                new ByteArrayInputStream(pdfByDynamicDocumentOutputStream.toByteArray()),
                formularDto.getDescription() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")) + ".pdf");
        final List<FileUploadTag> fileUploadTags = new ArrayList<>();
        if (formularDto.getTags() != null && !formularDto.getTags().isEmpty()) {
            int position = 0;
            for (final Map.Entry<String, String> documentTag : formularDto.getTags().entrySet()) {
                fileUploadTags.add(FileUploadTag.builder()
                        .name(documentTag.getKey())
                        .value(documentTag.getValue())
                        .fileUpload(newUpload)
                        .position(position++)
                        .build()
                );
            }
        }
        newUpload.setTags(fileUploadTags);
        newUpload.setDynamicDocument(new DynamicDocument(formularDto.getId()));
        this.fileUploadRepository.save(newUpload);
        return UploadDto.builder()
                .creationDate(newUpload.getCreationDate())
                .fileName(newUpload.getFileName())
                .uuid(newUpload.getUuid())
                .build();
    }

    @GetMapping(value = "/contexts/{context}/replacementtoken/text")
    public List<FormularReplacementTokenDto> getAllTextReplacementTokenPresets(@PathVariable("context") DocumentContextEnumDto context) {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        if (context == DocumentContextEnumDto.CUSTOMER) {
            for (final DocumentCustomerTextReplacementToken currentReplacementToken : DocumentCustomerTextReplacementToken.values()) {
                result.add(FormularReplacementTokenDto.builder()
                        .replacementToken(currentReplacementToken.getReplacementString())
                        .tokenName(currentReplacementToken.getTokenName())
                        .build());
            }
        }
        for (final DocumentTextReplacementToken currentReplacementToken : DocumentTextReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @GetMapping(value = "/contexts/{context}/replacementtoken/date")
    public List<FormularReplacementTokenDto> getAllDateReplacementTokenPresets(@PathVariable("context") DocumentContextEnumDto context) {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        for (final DocumentDateReplacementToken currentReplacementToken : DocumentDateReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @GetMapping(value = "/contexts/{context}/replacementtoken/checkbox")
    public List<FormularReplacementTokenDto> getAllCheckboxReplacementTokenPresets(@PathVariable("context") DocumentContextEnumDto context) {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        if (context == DocumentContextEnumDto.CUSTOMER) {
            for (final DocumentCustomerCheckboxReplacementToken currentReplacementToken : DocumentCustomerCheckboxReplacementToken.values()) {
                result.add(FormularReplacementTokenDto.builder()
                        .replacementToken(currentReplacementToken.getReplacementString())
                        .tokenName(currentReplacementToken.getTokenName())
                        .build());
            }
        }
        for (final DocumentCheckboxReplacementToken currentReplacementToken : DocumentCheckboxReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @GetMapping(value = "/contexts/{context}/replacementtoken/qrcode")
    public List<FormularReplacementTokenDto> getAllQrCodeReplacementTokenPresets(@PathVariable("context") DocumentContext context) {
        final List<FormularReplacementTokenDto> result = new ArrayList<>();
        for (final DocumentQrCodeReplacementToken currentReplacementToken : DocumentQrCodeReplacementToken.values()) {
            result.add(FormularReplacementTokenDto.builder()
                    .replacementToken(currentReplacementToken.getReplacementString())
                    .tokenName(currentReplacementToken.getTokenName())
                    .build());
        }
        return result;
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("id") Long id, @RequestBody FormularDto updatedDto) {
        final DynamicDocument model = this.transformer.transformToEntity(updatedDto);
        this.service.update(model);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void delete(@PathVariable("id") Long id) {
        final DynamicDocument model = this.service.findById(id);
        model.setDeleted(true);
        this.service.update(model);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public FormularDto create(@RequestBody FormularDto newDto) {
        final DynamicDocument model = this.transformer.transformToEntity(newDto);
        final DynamicDocument savedModel = this.service.create(model);
        return this.transformer.transformToDto(savedModel);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public FormularDto createDuplicate(@PathVariable("id") Long id) {
        final DynamicDocument documentToBeCopied = this.service.findById(id);
        final DynamicDocument documentCopy = new DynamicDocument(documentToBeCopied);
        final DynamicDocument savedModel = this.service.create(documentCopy);
        return this.transformer.transformToDto(savedModel);
    }


}
