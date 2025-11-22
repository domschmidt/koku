package de.domschmidt.koku.file.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file", schema = "koku")
public class File implements Serializable {

    @Id
    UUID id;
    boolean deleted;

    String filename;
    @Basic(fetch = LAZY)
    byte[] content;
    String mimeType;
    long size;

    @Enumerated(EnumType.STRING)
    FileRef ref;
    String refId;

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;

    public File(
            final UUID id,
            final String filename,
            final FileRef ref,
            final String refId,
            final String mimeType,
            final byte[] content,
            final long size
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.filename = filename;
        this.ref = ref;
        this.refId = refId;
        this.mimeType = mimeType;
        this.content = content;
        this.size = size;
    }
}
