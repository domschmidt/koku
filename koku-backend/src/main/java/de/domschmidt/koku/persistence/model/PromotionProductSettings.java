package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "promotion_product_settings", schema = "koku")
public class PromotionProductSettings extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    @OneToOne(cascade = CascadeType.ALL)
    Promotion promotion;
    BigDecimal absoluteSavings;
    BigDecimal relativeSavings;
    BigDecimal absoluteItemSavings;
    BigDecimal relativeItemSavings;

}
