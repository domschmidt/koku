package de.domschmidt.koku.activity.persistence;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "activity_price_history", schema = "koku")
public class ActivityPriceHistoryEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Version
    Long version;

    BigDecimal price;

    @ManyToOne
    Activity activity;

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;

    public ActivityPriceHistoryEntry(final Activity activity, final BigDecimal price) {
        this.price = price;
        this.activity = activity;
    }
}
