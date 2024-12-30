package de.domschmidt.koku.persistence.model;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import de.domschmidt.koku.persistence.model.common.DomainModel;
import de.domschmidt.koku.persistence.model.uploads.FileUpload;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(name = "customer", schema = "koku")
public class Customer extends DomainModel implements Serializable {
    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    @Expose
    Long id;
    @Expose
    String firstName;
    @Expose
    String lastName;
    @Expose
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
    boolean covid19boostered;
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
    LocalDateTime kafkaExported;

    @Override
    public String toString() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create().toJson(this);
    }
}
