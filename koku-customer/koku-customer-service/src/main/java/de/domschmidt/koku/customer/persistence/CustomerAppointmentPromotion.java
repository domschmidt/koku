package de.domschmidt.koku.customer.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_appointment_promotion", schema = "koku")
public class CustomerAppointmentPromotion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Version
    Long version;

    @ManyToOne
    CustomerAppointment appointment;

    Long promotionId;
    Integer position;

    public CustomerAppointmentPromotion(
            final CustomerAppointment appointment,
            final Long promotionId,
            final Integer position
    ) {
        this.appointment = appointment;
        this.promotionId = promotionId;
        this.position = position;
    }
}
