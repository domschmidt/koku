package de.domschmidt.koku.carddav.kafka.customers.dto;

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
public class CustomerDto {

    private Long id;
    private Boolean deleted;
    private LocalDateTime lastUpdated;

    private String firstName;
    private String lastName;
    private String email;
    private String initials;
    private String address;
    private String postalCode;
    private String city;
    private String privateTelephoneNo;
    private String businessTelephoneNo;
    private String mobileTelephoneNo;
    private String medicalTolerance;
    private String additionalInfo;
    private LocalDate birthday;
    private Boolean onFirstNameBasis;
    private Boolean hayFever;
    private Boolean plasterAllergy;
    private Boolean cyanoacrylateAllergy;
    private Boolean asthma;
    private Boolean dryEyes;
    private Boolean circulationProblems;
    private Boolean epilepsy;
    private Boolean diabetes;
    private Boolean claustrophobia;
    private Boolean neurodermatitis;
    private Boolean contacts;
    private Boolean glasses;
    private String eyeDisease;
    private String allergy;
    private Boolean covid19vaccinated;
    private Boolean covid19boostered;

}
