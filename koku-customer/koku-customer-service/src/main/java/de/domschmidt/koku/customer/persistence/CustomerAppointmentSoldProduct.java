package de.domschmidt.koku.customer.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_appointment_sold_product", schema = "koku")
public class CustomerAppointmentSoldProduct  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Version
    Long version;

    @ManyToOne
    CustomerAppointment appointment;

    Long productId;

    BigDecimal sellPrice;
    BigDecimal finalPriceSnapshot;
    Integer position;

    public CustomerAppointmentSoldProduct(
            final CustomerAppointment appointment,
            final Long productId,
            final BigDecimal sellPrice,
            final Integer position
    ) {
        this.appointment = appointment;
        this.productId = productId;
        this.sellPrice = sellPrice;
        this.position = position;
    }
}
