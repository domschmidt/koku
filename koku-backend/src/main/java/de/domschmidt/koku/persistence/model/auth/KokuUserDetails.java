package de.domschmidt.koku.persistence.model.auth;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "user_details",
        schema = "koku"
)
public class KokuUserDetails extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String firstname;
    String lastname;
    @Lob
    String avatarBase64;

    @OneToOne(cascade = CascadeType.ALL)
    KokuUser user;

}
