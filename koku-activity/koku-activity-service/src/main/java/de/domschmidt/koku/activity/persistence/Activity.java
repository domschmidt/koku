package de.domschmidt.koku.activity.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity", schema = "koku")
public class Activity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    boolean deleted;
    @Version
    Long version;

    String name = "";
    Duration approximatelyDuration;

    @OneToMany(mappedBy = "activity", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("recorded asc")
    List<ActivityPriceHistoryEntry> priceHistory = new ArrayList<>();

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;

}