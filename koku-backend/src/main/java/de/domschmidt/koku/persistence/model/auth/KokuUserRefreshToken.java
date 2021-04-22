package de.domschmidt.koku.persistence.model.auth;

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
@Table(name = "user_refresh_token", schema = "koku")
public class KokuUserRefreshToken extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    String tokenId;
    LocalDateTime expires;
    @ManyToOne
    KokuUser user;

}
