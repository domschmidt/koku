package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "private_appointment", schema = "koku")
public class PrivateAppointment extends DomainModel implements Serializable {
    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    LocalDateTime start;
    LocalDateTime ending;

    String description;
    boolean deleted;
    @ManyToOne
    KokuUser user;

}
