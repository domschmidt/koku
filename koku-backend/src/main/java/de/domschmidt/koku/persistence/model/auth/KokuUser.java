package de.domschmidt.koku.persistence.model.auth;

import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.PrivateAppointment;
import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "user",
        schema = "koku",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username"})
)
public class KokuUser extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String username;
    String password;
    boolean deleted;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    KokuUserDetails userDetails;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<PrivateAppointment> privateAppointments;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<CustomerAppointment> customerAppointments;

}
