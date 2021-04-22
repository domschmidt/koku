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
@Table(name = "customer_appointment_soldproducts_composing", schema = "koku")
public class CustomerAppointmentSoldProduct extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    @ManyToOne
    CustomerAppointment customerAppointment;

    @ManyToOne
    Product product;

    BigDecimal sellPrice;
    Integer position;

}
