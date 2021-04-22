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
@Table(name = "product_category", schema = "koku")
public class ProductCategory extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String description;

    @ManyToMany()
    @JoinTable(
            name = "product_category_composing",
            joinColumns = @JoinColumn(name = "productcategory_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"),
            schema = "koku"
    )
    List<Product> products;

}