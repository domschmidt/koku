package de.domschmidt.koku.customer.persistence;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(name = "customer", schema = "koku")
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    boolean deleted;

    @Version
    Long version;

    String firstname = "";
    String lastname = "";
    String email = "";
    String address = "";
    String postalCode = "";
    String city = "";
    String privateTelephoneNo = "";
    String businessTelephoneNo = "";
    String mobileTelephoneNo = "";
    String medicalTolerance = "";
    String additionalInfo = "";
    boolean onFirstnameBasis;
    boolean hayFever;
    boolean plasterAllergy;
    boolean cyanoacrylateAllergy;
    boolean asthma;
    boolean dryEyes;
    boolean circulationProblems;
    boolean epilepsy;
    boolean diabetes;
    boolean claustrophobia;
    boolean neurodermatitis;
    boolean contacts;
    boolean glasses;
    boolean covid19vaccinated;
    boolean covid19boostered;
    String eyeDisease = "";
    String allergy = "";
    LocalDate birthday;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @OrderBy("start DESC")
    List<CustomerAppointment> customerAppointments = new ArrayList<>();

    @CreationTimestamp
    LocalDateTime recorded;

    @UpdateTimestamp
    LocalDateTime updated;
}
