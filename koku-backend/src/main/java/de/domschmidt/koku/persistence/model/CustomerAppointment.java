package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_appointment", schema = "koku")
public class CustomerAppointment extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    LocalDateTime start;

    String description;
    String additionalInfo;

    @ManyToOne
    Customer customer;
    @OneToMany(orphanRemoval = true, mappedBy = "customerAppointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<CustomerAppointmentActivity> activities;
    @ManyToMany
    @JoinTable(
            name = "customer_appointment_sales_composing",
            joinColumns = @JoinColumn(name = "customer_appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "sale_id"),
            schema = "koku"
    )
    List<Sale> sales;
    @ManyToMany
    @JoinTable(
            name = "customer_appointment_promotions_composing",
            joinColumns = @JoinColumn(name = "customer_appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "promotion_id"),
            schema = "koku"
    )
    @OrderColumn
    List<Promotion> promotions;

    @OneToMany(orphanRemoval = true, mappedBy = "customerAppointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<CustomerAppointmentSoldProduct> soldProducts;

    @OneToMany(orphanRemoval = true, mappedBy = "customerAppointment", cascade = CascadeType.ALL)
    @OrderBy("position asc")
    List<ActivitySequenceItem> activitySequenceItems;

    @ManyToOne(fetch = FetchType.LAZY)
    KokuUser user;

}
