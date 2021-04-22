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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.io.FilenameUtils.getExtension;

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

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<Resource> getFile(@PathVariable("customerId") final Long customerId,
                                            @PathVariable("uuid") final UUID uuid) {
        final Customer customer = this.customerService.findById(customerId);
        final FileUpload uploadedFile = storageService.get(uuid);
        if (!customer.getUploads().contains(uploadedFile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        File file = new File(this.uploadConfiguration.getUploadsDir() + File.separator + uploadedFile.getUuid() + "." + getExtension(uploadedFile.getFileName()));

        final HttpHeaders header = new HttpHeaders();
        header.setContentDisposition(ContentDisposition.builder("attachment").filename(uploadedFile.getFileName(), StandardCharsets.UTF_8).build());
        // Disable caching
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity
                .ok().headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));
    }

}
