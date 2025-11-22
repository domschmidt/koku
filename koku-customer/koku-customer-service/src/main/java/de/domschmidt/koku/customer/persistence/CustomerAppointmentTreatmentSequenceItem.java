package de.domschmidt.koku.customer.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_appointment_activity_sequence", schema = "koku")
public class CustomerAppointmentTreatmentSequenceItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Version
    Long version;

    Long activityStepId;
    Long productId;

    @ManyToOne
    CustomerAppointment appointment;
    Integer position;

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;

    public CustomerAppointmentTreatmentSequenceItem(
            final CustomerAppointment appointment,
            final Long activityStepId,
            final Long productId,
            final Integer position
    ) {
        this.activityStepId = activityStepId;
        this.productId = productId;
        this.appointment = appointment;
        this.position = position;
    }
}
