package de.domschmidt.koku.product.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product", schema = "koku")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    boolean deleted;
    @Version
    Long version;

    String name = "";
    @ManyToOne(cascade = {
            CascadeType.ALL
    })
    ProductManufacturer manufacturer;

    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.ALL
            },
            orphanRemoval = true
    )
    @OrderBy("recorded asc")
    List<ProductPriceHistoryEntry> priceHistory = new ArrayList<>();

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;

}