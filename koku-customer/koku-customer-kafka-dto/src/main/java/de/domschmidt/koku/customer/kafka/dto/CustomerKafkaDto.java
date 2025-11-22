package de.domschmidt.koku.customer.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class CustomerKafkaDto {

    public static final String TOPIC = "customers";
    Long id;

    Boolean deleted;

    String firstname;
    String lastname;
    String fullname;
    String email;
    String address;
    String postalCode;
    String city;
    String privateTelephoneNo;
    String businessTelephoneNo;
    String mobileTelephoneNo;
    String medicalTolerance;
    String additionalInfo;
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
    String eyeDisease;
    String allergy;
    LocalDate birthday;

    LocalDateTime updated;
    LocalDateTime recorded;

}
