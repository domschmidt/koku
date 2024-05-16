package de.domschmidt.koku.controller.files;

import de.domschmidt.koku.dto.UploadDto;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.service.impl.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@Slf4j
public class FilesController {

    private final StorageService storageService;
    private final FileUploadRepository fileUploadRepository;

    @Autowired
    public FilesController(
            final StorageService storageService,
            final FileUploadRepository fileUploadRepository
    ) {
        this.storageService = storageService;
        this.fileUploadRepository = fileUploadRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadDto handleFileUpload(
            @RequestPart("file") final MultipartFile file
    ) throws IOException {
        final FileUpload uploadedFile = storageService.store(file);
        this.fileUploadRepository.save(uploadedFile);

        return UploadDto.builder()
                .creationDate(uploadedFile.getCreationDate())
                .fileName(uploadedFile.getFileName())
                .uuid(uploadedFile.getUuid())
                .build();
    }

    @DeleteMapping("/{uuid}")
    public void deleteFileUpload(
            @PathVariable("uuid") final UUID uuid
    ) {
        final Optional<FileUpload> optionalFileToBeDeleted = this.fileUploadRepository.findById(uuid);
        if (!optionalFileToBeDeleted.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final FileUpload fileTobeDeleted = optionalFileToBeDeleted.get();
        fileTobeDeleted.setDeleted(true);

        this.fileUploadRepository.save(fileTobeDeleted);
    }

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<Resource> getFile(
            @PathVariable("uuid") final UUID uuid
    ) {
        final File uploadedFile = storageService.getById(uuid);

        final HttpHeaders header = new HttpHeaders();
        header.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(uploadedFile.getName(), StandardCharsets.UTF_8)
                        .build()
        );
        // Disable caching
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        MediaType fileMediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            fileMediaType = MediaType.parseMediaType(new Tika().detect(uploadedFile));
        } catch (final Exception e) {
            // ignore errors
        }

        return ResponseEntity
                .ok().headers(header)
                .contentLength(uploadedFile.length())
                .contentType(fileMediaType)
                .body(new FileSystemResource(uploadedFile));
    }


}
