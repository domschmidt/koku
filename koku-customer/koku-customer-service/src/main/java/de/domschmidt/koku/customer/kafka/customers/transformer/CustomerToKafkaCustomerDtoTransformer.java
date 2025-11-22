package de.domschmidt.koku.customer.kafka.customers.transformer;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.customer.persistence.Customer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerToKafkaCustomerDtoTransformer {

    public CustomerKafkaDto transformToDto(final Customer model) {
        return CustomerKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .firstname(model.getFirstname())
                .lastname(model.getLastname())
                .fullname(Stream.of(model.getFirstname(), model.getLastname())
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(" ")))
                .email(model.getEmail())
                .address(model.getAddress())
                .postalCode(model.getPostalCode())
                .city(model.getCity())
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
}
