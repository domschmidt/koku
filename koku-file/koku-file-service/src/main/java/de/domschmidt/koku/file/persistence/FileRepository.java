package de.domschmidt.koku.file.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, UUID> {}
