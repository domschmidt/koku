package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product", schema = "koku")
public class Product extends DomainModel implements Comparable<Product>, Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String description;
    boolean deleted;

    @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
    List<ProductCategory> productCategories;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    @OrderBy("recorded asc")
    List<ProductPriceHistoryEntry> priceHistory;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    List<CustomerAppointmentSoldProduct> usageInSoldProducts;

    @OneToMany(mappedBy = "optionalProduct", fetch = FetchType.LAZY)
    List<ActivitySequenceItem> usageInActivitySequences;

    @ManyToOne
    ProductManufacturer manufacturer;

    @Override
    public int compareTo(Product o) {
        return Long.compare(this.id, o.getId());
    }
}
