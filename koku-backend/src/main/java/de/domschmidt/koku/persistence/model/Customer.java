package de.domschmidt.koku.persistence.model;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer", schema = "koku")
public class Customer extends DomainModel implements Serializable {
    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    String firstName;
    String lastName;
    String email;
    String address;
    String postalCode;
    String city;
    String privateTelephoneNo;
    String businessTelephoneNo;
    String mobileTelephoneNo;
    String medicalTolerance;
    String additionalInfo;
    boolean onFirstNameBasis;
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
    String eyeDisease;
    String allergy;
    boolean deleted;
    LocalDate birthday;
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    List<CustomerDocument> documents;
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @OrderBy("start DESC")
    List<CustomerAppointment> customerAppointments;
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    List<Sale> sales;
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    List<FileUpload> uploads;

}
