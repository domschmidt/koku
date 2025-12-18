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
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_appointment", schema = "koku")
public class CustomerAppointment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    boolean deleted;
    @Version
    Long version;

    LocalDateTime start;
    LocalDateTime calculatedEndSnapshot;

    String description = "";
    String additionalInfo = "";

    @ManyToOne
    Customer customer;
    String userId;
    @OneToMany(orphanRemoval = true, mappedBy = "appointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<CustomerAppointmentActivity> activities = new ArrayList<>();
    @OneToMany(orphanRemoval = true, mappedBy = "appointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<CustomerAppointmentPromotion> promotions = new ArrayList<>();
    @OneToMany(orphanRemoval = true, mappedBy = "appointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<CustomerAppointmentSoldProduct> soldProducts = new ArrayList<>();
    @OneToMany(orphanRemoval = true, mappedBy = "appointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<CustomerAppointmentTreatmentSequenceItem> treatmentSequence = new ArrayList<>();

    BigDecimal activitiesRevenueSnapshot;
    BigDecimal soldProductsRevenueSnapshot;

    @CreationTimestamp
    LocalDateTime recorded;
    @UpdateTimestamp
    LocalDateTime updated;
}
