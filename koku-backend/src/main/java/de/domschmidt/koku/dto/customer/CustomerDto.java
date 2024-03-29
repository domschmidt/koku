package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDto {

    private Long id;

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
    private boolean hayFever;
    private boolean plasterAllergy;
    private boolean cyanoacrylateAllergy;
    private boolean asthma;
    private boolean dryEyes;
    private boolean circulationProblems;
    private boolean epilepsy;
    private boolean diabetes;
    private boolean claustrophobia;
    private boolean neurodermatitis;
    private boolean contacts;
    private boolean glasses;
    private String eyeDisease;
    private String allergy;
    boolean covid19vaccinated;
    boolean covid19boostered;

}
