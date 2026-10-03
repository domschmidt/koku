package de.domschmidt.koku.promotion.persistence;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
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
@Table(name = "promotion", schema = "koku")
public class Promotion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    boolean deleted;

    @Version
    Long version;

    String name = "";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "promotion_manufacturer", schema = "koku", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "manufacturer_id", nullable = false)
    private Set<Long> productManufacturerIds = new LinkedHashSet<>();

    BigDecimal activityAbsoluteItemSavings;
    BigDecimal activityAbsoluteSavings;
    BigDecimal activityRelativeItemSavings;
    BigDecimal activityRelativeSavings;
    BigDecimal productAbsoluteItemSavings;
    BigDecimal productAbsoluteSavings;
    BigDecimal productRelativeItemSavings;
    BigDecimal productRelativeSavings;

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;
}
