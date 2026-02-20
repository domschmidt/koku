package de.domschmidt.koku.user.persistence;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
@Table(name = "user_appointment", schema = "koku")
public class UserAppointment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    boolean deleted;

    @Version
    Long version;

    LocalDateTime startTimestamp;
    LocalDateTime endTimestamp;

    String description = "";

    @ManyToOne
    User user;

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;
}
