package de.domschmidt.koku.persistence.model.uploads;

import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.dynamic_documents.DynamicDocument;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(name = "file", schema = "koku")
public class FileUpload implements Serializable {

    @Id
    UUID uuid;
    String fileName;
    LocalDateTime creationDate;
    Long size;
    boolean deleted;
    String mediaType;
    @ManyToOne(fetch = FetchType.LAZY)
    Customer customer;
    @ManyToOne(fetch = FetchType.LAZY)
    DynamicDocument dynamicDocument;
    @OneToMany(orphanRemoval = true, mappedBy = "fileUpload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("position asc")
    List<FileUploadTag> tags;

}
