package de.domschmidt.koku.product.persistence;

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
@Table(name = "product_price_history", schema = "koku")
public class ProductPriceHistoryEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    BigDecimal price;

    @ManyToOne
    Product product;

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;

    public ProductPriceHistoryEntry(final Product product, final BigDecimal price) {
        this.price = price;
        this.product = product;
    }
}
