package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuCustomerDto {

    Long id;
    Boolean deleted;
    Long version;

    String firstName;
    String lastName;
    String fullName;
    String fullNameWithOnFirstNameBasis;
    String initials;
    String email;
    String address;
    String postalCode;
    String city;
    String addressLine2;
    String privateTelephoneNo;
    String businessTelephoneNo;
    String mobileTelephoneNo;
    String medicalTolerance;
    String additionalInfo;
    LocalDate birthday;
    Boolean onFirstnameBasis;
    Boolean hayFever;
    Boolean plasterAllergy;
    Boolean cyanoacrylateAllergy;
    Boolean asthma;
    Boolean dryEyes;
    Boolean circulationProblems;
    Boolean epilepsy;
    Boolean diabetes;
    Boolean claustrophobia;
    Boolean neurodermatitis;
    Boolean contacts;
    Boolean glasses;
    String eyeDisease;
    String allergy;
    Boolean covid19vaccinated;
    Boolean covid19boostered;

    LocalDateTime updated;
    LocalDateTime recorded;

}
