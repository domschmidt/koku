package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity_sequence_item", schema = "koku")
public class ActivitySequenceItem extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    @ManyToOne
    ActivityStep optionalActivityStep;

    @ManyToOne
    Product optionalProduct;

    @ManyToOne
    CustomerAppointment customerAppointment;

    Integer position;

}
