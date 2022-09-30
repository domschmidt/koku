package de.domschmidt.koku.controller.customer;

import de.domschmidt.koku.configuration.UploadConfiguration;
import de.domschmidt.koku.dto.UploadDto;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.impl.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers/{customerId}/uploads")
@Slf4j
public class CustomerUploadsController {

    private final StorageService storageService;
    private final ICustomerService customerService;
    private final FileUploadRepository fileUploadRepository;
    private final UploadConfiguration uploadConfiguration;

    @Autowired
    public CustomerUploadsController(final StorageService storageService,
                                     final ICustomerService customerService,
                                     final FileUploadRepository fileUploadRepository,
                                     final UploadConfiguration uploadConfiguration
    ) {
        this.storageService = storageService;
        this.customerService = customerService;
        this.fileUploadRepository = fileUploadRepository;
        this.uploadConfiguration = uploadConfiguration;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadDto handleFileUpload(@RequestPart("file") final MultipartFile file,
                                      @PathVariable("customerId") final Long customerId) throws IOException {
        final Customer customer = this.customerService.findById(customerId);
        final FileUpload uploadedFile = storageService.store(file);
        uploadedFile.setCustomer(customer);
        this.fileUploadRepository.save(uploadedFile);

        return UploadDto.builder()
                .creationDate(uploadedFile.getCreationDate())
                .fileName(uploadedFile.getFileName())
                .uuid(uploadedFile.getUuid())
                .build();
    }

    @DeleteMapping("/{uuid}")
    public void deleteFileUpload(@PathVariable("customerId") final Long customerId,
                                 @PathVariable("uuid") final UUID uuid) {
        final Customer customer = this.customerService.findById(customerId);
        FileUpload uploadTobeDeleted = null;
        if (customer.getUploads() != null) {
            for (final FileUpload currentUpload : customer.getUploads()) {
                if (currentUpload.getUuid().equals(uuid)) {
                    uploadTobeDeleted = currentUpload;
                    break;
                }
            }
        }
        if (uploadTobeDeleted == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        } else {
            uploadTobeDeleted.setDeleted(true);
            this.fileUploadRepository.save(uploadTobeDeleted);
        }
    }

    @GetMapping()
    public List<UploadDto> getCustomerUploads(@PathVariable("customerId") final Long customerId) {
        final Customer customer = this.customerService.findById(customerId);
        final List<UploadDto> result = new ArrayList<>();
        for (final FileUpload currentUpload : customer.getUploads()) {
            if (!currentUpload.isDeleted()) {
                result.add(UploadDto.builder()
                        .creationDate(currentUpload.getCreationDate())
                        .fileName(currentUpload.getFileName())
                        .uuid(currentUpload.getUuid())
                        .build());
            }
        }
        return result;
    }

}
