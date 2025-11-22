package de.domschmidt.koku.customer.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_appointment_activity", schema = "koku")
public class CustomerAppointmentActivity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Version
    Long version;

    @ManyToOne
    CustomerAppointment appointment;

    Long activityId;

    BigDecimal sellPrice;
    BigDecimal finalPriceSnapshot;
    Integer position;

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;

    public CustomerAppointmentActivity(
            final CustomerAppointment appointment,
            final Long activityId,
            final BigDecimal sellPrice,
            final Integer position
    ) {
        this.appointment = appointment;
        this.activityId = activityId;
        this.sellPrice = sellPrice;
        this.position = position;
    }
}
