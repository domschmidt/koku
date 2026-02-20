package de.domschmidt.koku.activity.persistence;

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
@Table(name = "activity_step", schema = "koku")
public class ActivityStep implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Version
    Long version;

    String name = "";
    boolean deleted;

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;

    public ActivityStep(final Long id) {
        this.id = id;
    }
}
