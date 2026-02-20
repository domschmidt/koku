package de.domschmidt.koku.file.persistence;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Version
    Long version;

    String filename;

    @Basic(fetch = LAZY)
    byte[] content;

    String mimeType;
    long size;

    Long customerId;

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;

    public File(
            final UUID id,
            final String filename,
            final Long customerId,
            final String mimeType,
            final byte[] content,
            final long size) {
        this.id = id != null ? id : UUID.randomUUID();
        this.filename = filename;
        this.customerId = customerId;
        this.mimeType = mimeType;
        this.content = content;
        this.size = size;
    }
}
