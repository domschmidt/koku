package de.domschmidt.koku.persistence.model.uploads;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_tag", schema = "koku")
public class FileUploadTag implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String name;
    String value;
    Integer position;

    @ManyToOne
    FileUpload fileUpload;

}
