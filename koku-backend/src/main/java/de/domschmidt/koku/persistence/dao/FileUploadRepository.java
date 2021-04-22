package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileUploadRepository extends JpaRepository<FileUpload, UUID> {

}
