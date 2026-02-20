package de.domschmidt.koku.product.persistence;

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
@Table(name = "product_manufacturer", schema = "koku")
public class ProductManufacturer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    boolean deleted;

    @Version
    Long version;

    String name = "";

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;

    public ProductManufacturer(final Long id) {
        this.id = id;
    }
}
