package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.customer.CustomerDto;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomerToCustomerDtoTransformer implements ITransformer<Customer, CustomerDto> {

    public List<CustomerDto> transformToDtoList(final List<Customer> modelList) {
        final List<CustomerDto> result = new ArrayList<>();
        for (final Customer customer : modelList) {
            result.add(transformToDto(customer));
        }
        return result;
    }

    public CustomerDto transformToDto(final Customer model) {
        return CustomerDto.builder()
                .id(model.getId())
                .city(model.getCity())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .email(model.getEmail())
                .initials(transformInitials(model.getFirstName(), model.getLastName()))
                .address(model.getAddress())
                .privateTelephoneNo(model.getPrivateTelephoneNo())
                .businessTelephoneNo(model.getBusinessTelephoneNo())
                .mobileTelephoneNo(model.getMobileTelephoneNo())
                .postalCode(model.getPostalCode())
                .medicalTolerance(model.getMedicalTolerance())
                .additionalInfo(model.getAdditionalInfo())
                .birthday(model.getBirthday())
                .onFirstNameBasis(model.isOnFirstNameBasis())
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
                .eyeDisease(model.getEyeDisease())
                .allergy(model.getAllergy())
                .covid19vaccinated(model.isCovid19vaccinated())
                .build();
    }

    private String transformInitials(String firstName, String lastName) {
        String result = "";
        if (StringUtils.isNotEmpty(firstName)) {
            result += firstName.trim().substring(0, 1).toUpperCase();
        }
        if (StringUtils.isNotEmpty(lastName)) {
            result += lastName.trim().substring(0, 1).toUpperCase();
        }
        return result;
    }

    public Customer transformToEntity(final CustomerDto dtoModel) {
        return Customer.builder()
                .id(dtoModel.getId())
                .city(dtoModel.getCity())
                .firstName(dtoModel.getFirstName())
                .lastName(dtoModel.getLastName())
                .email(dtoModel.getEmail())
                .address(dtoModel.getAddress())
                .privateTelephoneNo(dtoModel.getPrivateTelephoneNo())
                .businessTelephoneNo(dtoModel.getBusinessTelephoneNo())
                .mobileTelephoneNo(dtoModel.getMobileTelephoneNo())
                .postalCode(dtoModel.getPostalCode())
                .medicalTolerance(dtoModel.getMedicalTolerance())
                .additionalInfo(dtoModel.getAdditionalInfo())
                .birthday(dtoModel.getBirthday())
                .onFirstNameBasis(Boolean.TRUE.equals(dtoModel.getOnFirstNameBasis()))
                .hayFever(Boolean.TRUE.equals(dtoModel.isHayFever()))
                .plasterAllergy(Boolean.TRUE.equals(dtoModel.isPlasterAllergy()))
                .cyanoacrylateAllergy(Boolean.TRUE.equals(dtoModel.isCyanoacrylateAllergy()))
                .asthma(Boolean.TRUE.equals(dtoModel.isAsthma()))
                .dryEyes(Boolean.TRUE.equals(dtoModel.isDryEyes()))
                .circulationProblems(Boolean.TRUE.equals(dtoModel.isCirculationProblems()))
                .epilepsy(Boolean.TRUE.equals(dtoModel.isEpilepsy()))
                .diabetes(Boolean.TRUE.equals(dtoModel.isDiabetes()))
                .claustrophobia(Boolean.TRUE.equals(dtoModel.isClaustrophobia()))
                .neurodermatitis(Boolean.TRUE.equals(dtoModel.isNeurodermatitis()))
                .contacts(Boolean.TRUE.equals(dtoModel.isContacts()))
                .glasses(Boolean.TRUE.equals(dtoModel.isGlasses()))
                .eyeDisease(dtoModel.getEyeDisease())
                .allergy(dtoModel.getAllergy())
                .covid19vaccinated(dtoModel.isCovid19vaccinated())
                .build();
    }
}
