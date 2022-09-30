package de.domschmidt.koku.controller.customer;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.controller.documents.DocumentPdfService;
import de.domschmidt.koku.dto.UploadDto;
import de.domschmidt.koku.dto.formular.FormularDto;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.persistence.model.uploads.FileUploadTag;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.impl.DocumentService;
import de.domschmidt.koku.service.impl.StorageService;
import de.domschmidt.koku.service.searchoptions.DocumentSearchOptions;
import de.domschmidt.koku.transformer.DynamicDocumentToFormularDtoTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/customers/{customerId}/documents")
@Slf4j
public class CustomerDocumentsController extends AbstractController<DynamicDocument, FormularDto, DocumentSearchOptions> {

    private final ICustomerService customerService;
    private final StorageService storageService;
    private final FileUploadRepository fileUploadRepository;
    private final DynamicDocumentToFormularDtoTransformer transformer;
    private final DocumentPdfService documentPdfService;

    @Autowired
    public CustomerDocumentsController(
            final ICustomerService customerService,
            final StorageService storageService,
            final FileUploadRepository fileUploadRepository,
            final DocumentService documentService,
            final DynamicDocumentToFormularDtoTransformer transformer,
            final DocumentPdfService documentPdfService
    ) {
        super(documentService, transformer);
        this.customerService = customerService;
        this.transformer = transformer;
        this.storageService = storageService;
        this.fileUploadRepository = fileUploadRepository;
        this.documentPdfService = documentPdfService;
    }

    @GetMapping(value = "/{id}")
    public FormularDto findByIdTransformed(@PathVariable("id") Long id, @PathVariable("customerId") Long customerId) {
        final Customer customer = this.customerService.findById(customerId);
        final DynamicDocument document = findById(id);
        if (document == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Map<String, Object> context = new HashMap<>();
        context.put("customer", customer);

        return this.transformer.transformToDto(document, context);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadDto createByOpenPdf(
            @RequestBody final FormularDto formularDto,
            @PathVariable("customerId") Long customerId
    ) throws IOException {
        final Customer customer = this.customerService.findById(customerId);

        final ByteArrayOutputStream pdfByDynamicDocumentOutputStream =
                this.documentPdfService.createPdfByDynamicDocument(formularDto);

        final FileUpload newUpload = this.storageService.store(
                new ByteArrayInputStream(pdfByDynamicDocumentOutputStream.toByteArray()),
                formularDto.getDescription() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")) + ".pdf");
        if (customer.getUploads() == null) {
            customer.setUploads(new ArrayList<>());
        }
        newUpload.setCustomer(customer);
        newUpload.setDynamicDocument(new DynamicDocument(formularDto.getId()));
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
        this.fileUploadRepository.save(newUpload);
        return UploadDto.builder()
                .creationDate(newUpload.getCreationDate())
                .fileName(newUpload.getFileName())
                .uuid(newUpload.getUuid())
                .build();
    }

}
