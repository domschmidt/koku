package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CustomerToCustomerDtoTransformer {

    public KokuCustomerDto transformToDto(final Customer model) {
        return KokuCustomerDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .firstName(model.getFirstname())
                .lastName(model.getLastname())
                .fullName(
                        Stream.of(model.getFirstname(), model.getLastname())
                                .filter(s -> s != null && !s.isEmpty())
                                .collect(Collectors.joining(" "))
                )
                .fullNameWithOnFirstNameBasis(
                        Stream.of(model.getFirstname(), model.getLastname())
                                .filter(s -> s != null && !s.isEmpty())
                                .collect(Collectors.joining(" "))
                                + (model.isOnFirstnameBasis() ? " *" : "")
                )
                .initials(
                        (model.getFirstname() != null ? StringUtils.truncate(model.getFirstname(), 1) : "") +
                                (model.getLastname() != null ? StringUtils.truncate(model.getLastname(), 1) : "")
                )
                .email(model.getEmail())
                .address(model.getAddress())
                .postalCode(model.getPostalCode())
                .city(model.getCity())
                .addressLine2(Stream.of(model.getPostalCode(), model.getCity())
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(" "))
                )
                .privateTelephoneNo(model.getPrivateTelephoneNo())
                .businessTelephoneNo(model.getBusinessTelephoneNo())
                .mobileTelephoneNo(model.getMobileTelephoneNo())
                .medicalTolerance(model.getMedicalTolerance())
                .additionalInfo(model.getAdditionalInfo())
                .onFirstnameBasis(model.isOnFirstnameBasis())
                .hayFever(model.isHayFever())
                .plasterAllergy(model.isPlasterAllergy())
                .cyanoacrylateAllergy(model.isCyanoacrylateAllergy())
                .asthma(model.isAsthma())
                .dryEyes(model.isDryEyes())
                .circulationProblems(model.isCirculationProblems())
                .epilepsy(model.isEpilepsy())
                .diabetes(model.isDiabetes())
                .claustrophobia(model.isClaustrophobia())
                .neurodermatitis(model.isNeurodermatitis())
                .contacts(model.isContacts())
                .glasses(model.isGlasses())
                .covid19vaccinated(model.isCovid19vaccinated())
                .covid19boostered(model.isCovid19boostered())
                .eyeDisease(model.getEyeDisease())
                .allergy(model.getAllergy())
                .birthday(model.getBirthday())
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

    public Customer transformToEntity(
            final Customer model,
            final KokuCustomerDto updatedDto
    ) {

        if (updatedDto.getFirstName() != null) {
            model.setFirstname(updatedDto.getFirstName());
        }
        if (updatedDto.getLastName() != null) {
            model.setLastname(updatedDto.getLastName());
        }
        if (updatedDto.getEmail() != null) {
            model.setEmail(updatedDto.getEmail());
        }
        if (updatedDto.getEmail() != null) {
            model.setEmail(updatedDto.getEmail());
        }
        if (updatedDto.getAddress() != null) {
            model.setAddress(updatedDto.getAddress());
        }
        if (updatedDto.getPostalCode() != null) {
            model.setPostalCode(updatedDto.getPostalCode());
        }
        if (updatedDto.getCity() != null) {
            model.setCity(updatedDto.getCity());
        }
        if (updatedDto.getPrivateTelephoneNo() != null) {
            model.setPrivateTelephoneNo(updatedDto.getPrivateTelephoneNo());
        }
        if (updatedDto.getBusinessTelephoneNo() != null) {
            model.setBusinessTelephoneNo(updatedDto.getBusinessTelephoneNo());
        }
        if (updatedDto.getMobileTelephoneNo() != null) {
            model.setMobileTelephoneNo(updatedDto.getMobileTelephoneNo());
        }
        if (updatedDto.getMedicalTolerance() != null) {
            model.setMedicalTolerance(updatedDto.getMedicalTolerance());
        }
        if (updatedDto.getAdditionalInfo() != null) {
            model.setAdditionalInfo(updatedDto.getAdditionalInfo());
        }
        if (updatedDto.getBirthday() != null) {
            model.setBirthday(updatedDto.getBirthday());
        }
        if (updatedDto.getOnFirstnameBasis() != null) {
            model.setOnFirstnameBasis(Boolean.TRUE.equals(updatedDto.getOnFirstnameBasis()));
        }
        if (updatedDto.getHayFever() != null) {
            model.setHayFever(Boolean.TRUE.equals(updatedDto.getHayFever()));
        }
        if (updatedDto.getPlasterAllergy() != null) {
            model.setPlasterAllergy(Boolean.TRUE.equals(updatedDto.getPlasterAllergy()));
        }
        if (updatedDto.getCyanoacrylateAllergy() != null) {
            model.setCyanoacrylateAllergy(Boolean.TRUE.equals(updatedDto.getCyanoacrylateAllergy()));
        }
        if (updatedDto.getAsthma() != null) {
            model.setAsthma(Boolean.TRUE.equals(updatedDto.getAsthma()));
        }
        if (updatedDto.getDryEyes() != null) {
            model.setDryEyes(Boolean.TRUE.equals(updatedDto.getDryEyes()));
        }
        if (updatedDto.getCirculationProblems() != null) {
            model.setCirculationProblems(Boolean.TRUE.equals(updatedDto.getCirculationProblems()));
        }
        if (updatedDto.getEpilepsy() != null) {
            model.setEpilepsy(Boolean.TRUE.equals(updatedDto.getEpilepsy()));
        }
        if (updatedDto.getDiabetes() != null) {
            model.setDiabetes(Boolean.TRUE.equals(updatedDto.getDiabetes()));
        }
        if (updatedDto.getClaustrophobia() != null) {
            model.setClaustrophobia(Boolean.TRUE.equals(updatedDto.getClaustrophobia()));
        }
        if (updatedDto.getNeurodermatitis() != null) {
            model.setNeurodermatitis(Boolean.TRUE.equals(updatedDto.getNeurodermatitis()));
        }
        if (updatedDto.getContacts() != null) {
            model.setContacts(Boolean.TRUE.equals(updatedDto.getContacts()));
        }
        if (updatedDto.getGlasses() != null) {
            model.setGlasses(Boolean.TRUE.equals(updatedDto.getGlasses()));
        }
        if (updatedDto.getEyeDisease() != null) {
            model.setEyeDisease(updatedDto.getEyeDisease());
        }
        if (updatedDto.getAllergy() != null) {
            model.setAllergy(updatedDto.getAllergy());
        }
        if (updatedDto.getAllergy() != null) {
            model.setAllergy(updatedDto.getAllergy());
        }
        if (updatedDto.getCovid19vaccinated() != null) {
            model.setCovid19vaccinated(Boolean.TRUE.equals(updatedDto.getCovid19vaccinated()));
        }
        if (updatedDto.getCovid19boostered() != null) {
            model.setCovid19boostered(Boolean.TRUE.equals(updatedDto.getCovid19boostered()));
        }

        return model;
    }
}
