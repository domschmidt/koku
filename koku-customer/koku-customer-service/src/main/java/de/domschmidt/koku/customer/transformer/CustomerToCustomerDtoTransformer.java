package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.service.PhoneNumberNormalizer;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerToCustomerDtoTransformer {
    private static final String ADDRESS_PART_SEPARATOR = " ";

    private final PhoneNumberNormalizer phoneNumberNormalizer;

    public KokuCustomerDto transformToDto(final Customer model) {
        return KokuCustomerDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .firstName(model.getFirstname())
                .lastName(model.getLastname())
                .fullName(CustomerNameFormatter.displayName(model))
                .fullNameWithOnFirstNameBasis(CustomerNameFormatter.displayNameWithFirstnameBasisMarker(model))
                .initials(CustomerNameFormatter.initials(model))
                .email(model.getEmail())
                .address(model.getAddress())
                .postalCode(model.getPostalCode())
                .city(model.getCity())
                .addressLine2(formatAddressLine(model))
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

    public Customer transformToEntity(final Customer model, final KokuCustomerDto updatedDto) {
        updateIdentityAndAddress(model, updatedDto);
        updatePhoneNumbers(model, updatedDto);
        updateNotesAndBirthday(model, updatedDto);
        updateBooleanProfileFields(model, updatedDto);
        updateMedicalTextFields(model, updatedDto);
        updateDeletionState(model, updatedDto);

        return model;
    }

    private void updateIdentityAndAddress(final Customer model, final KokuCustomerDto updatedDto) {
        if (updatedDto.getFirstName() != null) {
            model.setFirstname(updatedDto.getFirstName());
        }
        if (updatedDto.getLastName() != null) {
            model.setLastname(updatedDto.getLastName());
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
    }

    private void updatePhoneNumbers(final Customer model, final KokuCustomerDto updatedDto) {
        if (updatedDto.getPrivateTelephoneNo() != null) {
            model.setPrivateTelephoneNo(phoneNumberNormalizer.normalize(updatedDto.getPrivateTelephoneNo()));
        }
        if (updatedDto.getBusinessTelephoneNo() != null) {
            model.setBusinessTelephoneNo(phoneNumberNormalizer.normalize(updatedDto.getBusinessTelephoneNo()));
        }
        if (updatedDto.getMobileTelephoneNo() != null) {
            model.setMobileTelephoneNo(phoneNumberNormalizer.normalize(updatedDto.getMobileTelephoneNo()));
        }
    }

    private static void updateNotesAndBirthday(final Customer model, final KokuCustomerDto updatedDto) {
        if (updatedDto.getMedicalTolerance() != null) {
            model.setMedicalTolerance(updatedDto.getMedicalTolerance());
        }
        if (updatedDto.getAdditionalInfo() != null) {
            model.setAdditionalInfo(updatedDto.getAdditionalInfo());
        }
        if (updatedDto.getBirthday() != null) {
            model.setBirthday(updatedDto.getBirthday());
        }
    }

    private static void updateBooleanProfileFields(final Customer model, final KokuCustomerDto updatedDto) {
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
        if (updatedDto.getCovid19vaccinated() != null) {
            model.setCovid19vaccinated(Boolean.TRUE.equals(updatedDto.getCovid19vaccinated()));
        }
        if (updatedDto.getCovid19boostered() != null) {
            model.setCovid19boostered(Boolean.TRUE.equals(updatedDto.getCovid19boostered()));
        }
    }

    private static void updateMedicalTextFields(final Customer model, final KokuCustomerDto updatedDto) {
        if (updatedDto.getEyeDisease() != null) {
            model.setEyeDisease(updatedDto.getEyeDisease());
        }
        if (updatedDto.getAllergy() != null) {
            model.setAllergy(updatedDto.getAllergy());
        }
    }

    private static void updateDeletionState(final Customer model, final KokuCustomerDto updatedDto) {
        if (updatedDto.getDeleted() != null) {
            model.setDeleted(updatedDto.getDeleted());
        }
    }

    private static String formatAddressLine(final Customer model) {
        return Arrays.stream(new String[] {model.getPostalCode(), model.getCity()})
                .filter(Objects::nonNull)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(ADDRESS_PART_SEPARATOR));
    }
}
