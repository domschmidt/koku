package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.configuration.UploadConfiguration;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.io.FilenameUtils.getExtension;

@Service
@Transactional
public class StorageService {

    private final FileUploadRepository fileUploadRepository;
    private final UploadConfiguration uploadConfiguration;

    public StorageService(final FileUploadRepository fileUploadRepository,
                          final UploadConfiguration uploadConfiguration) {
        this.fileUploadRepository = fileUploadRepository;
        this.uploadConfiguration = uploadConfiguration;
    }

    public FileUpload store(final MultipartFile file) throws IOException {
        final UUID uuid = UUID.randomUUID();
        final String originalFilename = file.getOriginalFilename();
        final String fileExtension = getExtension(originalFilename);
        final String newFilePath = this.uploadConfiguration.getUploadsDir() + File.separator + uuid + "." + fileExtension;
        final File serverFile = new File(newFilePath);
        file.transferTo(serverFile);
        final FileUpload fileUpload;
        try {
            fileUpload = new FileUpload();
            fileUpload.setFileName(originalFilename);
            fileUpload.setUuid(uuid);
            fileUpload.setCreationDate(LocalDateTime.now());
            fileUpload.setSize(file.getSize());
            fileUploadRepository.save(fileUpload);
        } catch (final Exception e) {
            Files.delete(serverFile.toPath());
            throw e;
        }
        return fileUpload;
    }

    public File getById(final UUID uuid) {
        final Optional<FileUpload> fetchedFileUpload = this.fileUploadRepository.findById(uuid);
        if (fetchedFileUpload.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File Upload nicht gefunden");
        } else {
            final FileUpload fileUpload = fetchedFileUpload.get();
            return getFileByFileUpload(fileUpload);
        }
    }

    public File getFileByFileUpload(
            final FileUpload fileUpload
    ) {
        return new File(this.uploadConfiguration.getUploadsDir() + File.separator + fileUpload.getUuid() + "." + getExtension(fileUpload.getFileName()));
    }

    public FileUpload store(final ByteArrayInputStream byteArrayInputStream, final String fileName) throws IOException {
        final UUID uuid = UUID.randomUUID();
        final String fileExtension = getExtension(fileName);
        final String newFilePath = this.uploadConfiguration.getUploadsDir() + File.separator + uuid + "." + fileExtension;
        final File serverFile = new File(newFilePath);
        IOUtils.copy(byteArrayInputStream, new FileOutputStream(serverFile));
        final FileUpload fileUpload;
        try {
            fileUpload = new FileUpload();
            fileUpload.setFileName(fileName);
            fileUpload.setUuid(uuid);
            fileUpload.setCreationDate(LocalDateTime.now());
            fileUpload.setSize(serverFile.length());
            fileUploadRepository.save(fileUpload);
        } catch (final Exception e) {
            Files.delete(serverFile.toPath());
            throw e;
        }
        return fileUpload;
    }
}
