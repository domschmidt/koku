package de.domschmidt.koku.user.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", schema = "koku")
@FieldNameConstants
public class User implements Serializable {

    @Id
    String id;
    @Version
    Long version;

    boolean deleted;
    String firstname = "";
    String lastname = "";
    String fullname = "";

    String avatarBase64;
    @ManyToOne
    UserRegion region;

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;

    public User(
            final String firstname,
            final String lastname,
            final String avatarBase64
    ) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.avatarBase64 = avatarBase64;
    }

    public User(
            final String id
    ) {
        this.id = id;
    }
}
