package de.domschmidt.koku.persistence.model.uploads;

import de.domschmidt.koku.persistence.model.Customer;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file", schema = "koku")
public class FileUpload implements Serializable {

    @Id
    UUID uuid;
    String fileName;
    LocalDateTime creationDate;
    boolean deleted;

    @ManyToOne
    Customer customer;

}
