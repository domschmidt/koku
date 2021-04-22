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
@Table(name = "product_manufacturer", schema = "koku")
public class ProductManufacturer extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String name;
    boolean deleted;

    @OneToMany(mappedBy = "manufacturer", fetch = FetchType.LAZY)
    @OrderBy("recorded ASC")
    List<Product> products;

}
