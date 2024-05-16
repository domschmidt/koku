package de.domschmidt.koku.init;

import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.koku.persistence.dao.FileUploadRepository;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import de.domschmidt.koku.persistence.model.uploads.QFileUpload;
import de.domschmidt.koku.service.impl.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.File;
import java.util.List;

@Component
@Slf4j
public class FileUploadSizeInitializr {

    private final StorageService storageService;
    private final EntityManager entityManager;
    private final FileUploadRepository fileUploadRepository;

    @Autowired
    public FileUploadSizeInitializr(
            final EntityManager entityManager,
            final StorageService storageService,
            final FileUploadRepository fileUploadRepository
    ) {
        this.entityManager = entityManager;
        this.storageService = storageService;
        this.fileUploadRepository = fileUploadRepository;
    }

    /**
     * Updates the filesize of all stored files
     */
    @PostConstruct
    @Transactional()
    public void init() {
        final QFileUpload qFileUpload = QFileUpload.fileUpload;
        final List<FileUpload> allFiles = new JPAQuery<>(this.entityManager)
                .select(qFileUpload)
                .from(qFileUpload)
                .where(qFileUpload.deleted.ne(true))
                .fetch();
        for (final FileUpload currentFile : allFiles) {
            final File storedFile = storageService.getFileByFileUpload(currentFile);
            if (storedFile.exists()) {
                final Long fileSystemSize = storedFile.length();
                if (!fileSystemSize.equals(currentFile.getSize())) {
                    currentFile.setSize(fileSystemSize);
                }
                MediaType fileMediaType = MediaType.APPLICATION_OCTET_STREAM;
                try {
                    fileMediaType = MediaType.parseMediaType(new Tika().detect(storedFile));
                } catch (final Exception e) {
                    // ignore errors
                }
                if (!fileMediaType.toString().equals(currentFile.getMediaType())) {
                    currentFile.setMediaType(fileMediaType.toString());
                }
            } else {
                currentFile.setSize(0L);
                currentFile.setMediaType(null);
            }
        }
        this.fileUploadRepository.saveAll(allFiles);
    }

}

