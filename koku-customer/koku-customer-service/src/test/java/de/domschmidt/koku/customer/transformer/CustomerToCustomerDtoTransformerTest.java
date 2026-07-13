package de.domschmidt.koku.customer.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.service.PhoneNumberNormalizer;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CustomerToCustomerDtoTransformerTest {

    private final PhoneNumberNormalizer normalizer = mock(PhoneNumberNormalizer.class);
    private final CustomerToCustomerDtoTransformer transformer = new CustomerToCustomerDtoTransformer(normalizer);

    @Test
    void transformToDtoMapsIdentityAddressAndProfile() {
        final Customer customer = new Customer();
        customer.setId(7L);
        customer.setVersion(3L);
        customer.setFirstname("Ada");
        customer.setLastname("Lovelace");
        customer.setPostalCode("12345");
        customer.setCity("London");
        customer.setEmail("ada@example.test");
        customer.setOnFirstnameBasis(true);
        customer.setAsthma(true);
        customer.setBirthday(LocalDate.of(1815, java.time.Month.DECEMBER, 10));

        final KokuCustomerDto dto = transformer.transformToDto(customer);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getVersion()).isEqualTo(3L);
        assertThat(dto.getFullName()).contains("Ada", "Lovelace");
        assertThat(dto.getInitials()).isEqualTo("AL");
        assertThat(dto.getAddressLine2()).isEqualTo("12345 London");
        assertThat(dto.getOnFirstnameBasis()).isTrue();
        assertThat(dto.getAsthma()).isTrue();
        assertThat(dto.getBirthday()).isEqualTo(LocalDate.of(1815, java.time.Month.DECEMBER, 10));
    }

    @Test
    void transformToDtoOmitsMissingAddressPartsCleanly() {
        final Customer customer = new Customer();
        customer.setCity("Berlin");

        assertThat(transformer.transformToDto(customer).getAddressLine2()).isEqualTo("Berlin");
        customer.setCity("");
        assertThat(transformer.transformToDto(customer).getAddressLine2()).isEmpty();
    }

    @Test
    void fullUpdateNormalizesPhonesAndAppliesAllProfileFields() {
        when(normalizer.normalize("private")).thenReturn("p-normalized");
        when(normalizer.normalize("business")).thenReturn("b-normalized");
        when(normalizer.normalize("mobile")).thenReturn("m-normalized");
        final Customer customer = new Customer();
        final LocalDate birthday = LocalDate.of(1990, java.time.Month.APRIL, 3);
        final KokuCustomerDto update = KokuCustomerDto.builder()
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@example.test")
                .address("Main Street 1")
                .postalCode("54321")
                .city("Arlington")
                .privateTelephoneNo("private")
                .businessTelephoneNo("business")
                .mobileTelephoneNo("mobile")
                .medicalTolerance("none")
                .additionalInfo("note")
                .birthday(birthday)
                .onFirstnameBasis(true)
                .hayFever(true)
                .plasterAllergy(true)
                .cyanoacrylateAllergy(true)
                .asthma(true)
                .dryEyes(true)
                .circulationProblems(true)
                .epilepsy(true)
                .diabetes(true)
                .claustrophobia(true)
                .neurodermatitis(true)
                .contacts(true)
                .glasses(true)
                .covid19vaccinated(true)
                .covid19boostered(true)
                .eyeDisease("short sighted")
                .allergy("pollen")
                .deleted(true)
                .build();

        transformer.transformToEntity(customer, update);

        assertThat(customer.getFirstname()).isEqualTo("Grace");
        assertThat(customer.getLastname()).isEqualTo("Hopper");
        assertThat(customer.getPrivateTelephoneNo()).isEqualTo("p-normalized");
        assertThat(customer.getBusinessTelephoneNo()).isEqualTo("b-normalized");
        assertThat(customer.getMobileTelephoneNo()).isEqualTo("m-normalized");
        assertThat(customer.getBirthday()).isEqualTo(birthday);
        assertThat(customer)
                .returns(true, Customer::isOnFirstnameBasis)
                .returns(true, Customer::isHayFever)
                .returns(true, Customer::isPlasterAllergy)
                .returns(true, Customer::isCyanoacrylateAllergy)
                .returns(true, Customer::isAsthma)
                .returns(true, Customer::isDryEyes)
                .returns(true, Customer::isCirculationProblems)
                .returns(true, Customer::isEpilepsy)
                .returns(true, Customer::isDiabetes)
                .returns(true, Customer::isClaustrophobia)
                .returns(true, Customer::isNeurodermatitis)
                .returns(true, Customer::isContacts)
                .returns(true, Customer::isGlasses)
                .returns(true, Customer::isCovid19vaccinated)
                .returns(true, Customer::isCovid19boostered)
                .returns(true, Customer::isDeleted);
        assertThat(customer.getEyeDisease()).isEqualTo("short sighted");
        assertThat(customer.getAllergy()).isEqualTo("pollen");
    }

    @Test
    void absentFieldsPreserveExistingValues() {
        final Customer customer = new Customer();
        customer.setFirstname("Existing");
        customer.setAsthma(true);
        customer.setMobileTelephoneNo("existing-number");

        transformer.transformToEntity(customer, KokuCustomerDto.builder().build());

        assertThat(customer.getFirstname()).isEqualTo("Existing");
        assertThat(customer.isAsthma()).isTrue();
        assertThat(customer.getMobileTelephoneNo()).isEqualTo("existing-number");
    }

    @Test
    void explicitFalseClearsBooleanProfileFields() {
        final Customer customer = new Customer();
        customer.setAsthma(true);
        customer.setGlasses(true);

        transformer.transformToEntity(
                customer, KokuCustomerDto.builder().asthma(false).glasses(false).build());

        assertThat(customer.isAsthma()).isFalse();
        assertThat(customer.isGlasses()).isFalse();
    }
}
