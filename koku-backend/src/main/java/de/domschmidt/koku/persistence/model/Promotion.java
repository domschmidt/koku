package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "promotion", schema = "koku")
public class Promotion extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String name;
    boolean deleted;
    LocalDate startDate;
    LocalDate endDate;
    @OneToOne(cascade = CascadeType.ALL)
    PromotionProductSettings promotionProductSettings;
    @OneToOne(cascade = CascadeType.ALL)
    PromotionActivitySettings promotionActivitySettings;

}
