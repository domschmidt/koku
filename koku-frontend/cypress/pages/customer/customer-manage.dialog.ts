import {KokuDialog} from "../../page-helper/dialogs/koku.dialog";
import {padStart} from "lodash";

export class CustomerManageDialog implements KokuDialog {

  private readonly SELECTOR_CUSTOMER_FORM = '#customer-details';

  private readonly SELECTOR_FIRSTNAME_FIELD = '#customer-details__firstname';
  private readonly SELECTOR_LASTNAME_FIELD = '#customer-details__lastname';
  private readonly SELECTOR_EMAIL_FIELD = '#customer-details__email';
  private readonly SELECTOR_ADDRESS_FIELD = '#customer-details__address';
  private readonly SELECTOR_POSTALCODE_FIELD = '#customer-details__postal-code';
  private readonly SELECTOR_CITY_FIELD = '#customer-details__city';
  private readonly SELECTOR_COVID_19_VACCINATED_CHECKBOX = '#customer-details__covid19vaccinated-input';
  private readonly SELECTOR_GLASSES_CHECKBOX = '#customer-details__glasses-input';
  private readonly SELECTOR_CONTACTS_CHECKBOX = '#customer-details__contacts-input';
  private readonly SELECTOR_NEURODERMATITIS_CHECKBOX = '#customer-details__neurodermatitis-input';
  private readonly SELECTOR_ON_FIRST_NAME_BASIS_CHECKBOX = '#customer-details__on-first-name-basis-input';
  private readonly SELECTOR_CLAUSTROPHOBIA_CHECKBOX = '#customer-details__claustrophobia-input';
  private readonly SELECTOR_DIABETES_CHECKBOX = '#customer-details__diabetes-input';
  private readonly SELECTOR_EPILEPSY_CHECKBOX = '#customer-details__epilepsy-input';
  private readonly SELECTOR_CIRCULATION_PROBLEMS_CHECKBOX = '#customer-details__circulation-problems-input';
  private readonly SELECTOR_EYE_DISEASE_FIELD = '#customer-details__eye-disease-info';
  private readonly SELECTOR_DRY_EYES_CHECKBOX = '#customer-details__dry-eyes-input';
  private readonly SELECTOR_ASTHMA_CHECKBOX = '#customer-details__asthma-input';
  private readonly SELECTOR_ALLERGY_FIELD = '#customer-details__allergy-info';
  private readonly SELECTOR_CYANOACRYLATE_ALLERGY_CHECKBOX = '#customer-details__cyanoacrylate-allergy-input';
  private readonly SELECTOR_PLASTER_ALLERGY_CHECKBOX = '#customer-details__plaster-allergy-input';
  private readonly SELECTOR_HAY_FEVER_CHECKBOX = '#customer-details__hay-fever-input';
  private readonly SELECTOR_MEDICAL_INFO_FIELD = '#customer-details__medical-info';
  private readonly SELECTOR_ADDITIONAL_INFO_FIELD = '#customer-details__additional-info';
  private readonly SELECTOR_BIRTHDAY_FIELD = '#customer-details__birthday';
  private readonly SELECTOR_BUSINESS_PHONE_FIELD = '#customer-details__business-phone';
  private readonly SELECTOR_MOBILE_PHONE_FIELD = '#customer-details__mobile-phone';
  private readonly SELECTOR_PRIVATE_PHONE_FIELD = '#customer-details__private-phone';

  validateDialogOpened(): void {
    cy.get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_FIRSTNAME_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_LASTNAME_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_EMAIL_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_ADDRESS_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_POSTALCODE_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_CITY_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_COVID_19_VACCINATED_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_GLASSES_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_CONTACTS_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_NEURODERMATITIS_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_CLAUSTROPHOBIA_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_DIABETES_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_EPILEPSY_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_CIRCULATION_PROBLEMS_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_EYE_DISEASE_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_DRY_EYES_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_ASTHMA_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_ALLERGY_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_CYANOACRYLATE_ALLERGY_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_PLASTER_ALLERGY_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_HAY_FEVER_CHECKBOX)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_MEDICAL_INFO_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_ADDITIONAL_INFO_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_BIRTHDAY_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_BUSINESS_PHONE_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_MOBILE_PHONE_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_PRIVATE_PHONE_FIELD)
      .get(this.SELECTOR_CUSTOMER_FORM).find(this.SELECTOR_ON_FIRST_NAME_BASIS_CHECKBOX);
  }

  writeFirstName(value: string) {
    return cy.get(this.SELECTOR_FIRSTNAME_FIELD).clear().type(value);
  }

  writeLastName(value: string) {
    return cy.get(this.SELECTOR_LASTNAME_FIELD).clear().type(value);
  }

  writeEmail(value: string) {
    return cy.get(this.SELECTOR_EMAIL_FIELD).clear().type(value);
  }

  writeAddress(value: string) {
    return cy.get(this.SELECTOR_ADDRESS_FIELD).clear().type(value);
  }

  writePostalCode(value: number) {
    return cy.get(this.SELECTOR_POSTALCODE_FIELD).clear().type(String(value));
  }

  writeCity(value: string) {
    return cy.get(this.SELECTOR_CITY_FIELD).clear().type(value);
  }

  writeEyeDisease(value: string) {
    return cy.get(this.SELECTOR_EYE_DISEASE_FIELD).clear().type(value);
  }

  writeAllergy(value: string) {
    return cy.get(this.SELECTOR_ALLERGY_FIELD).clear().type(value);
  }

  writeMedicalInfo(value: string) {
    return cy.get(this.SELECTOR_MEDICAL_INFO_FIELD).clear().type(value);
  }

  writeAdditionalInfo(value: string) {
    return cy.get(this.SELECTOR_ADDITIONAL_INFO_FIELD).clear().type(value);
  }

  writeBirthday(value: Date) {
    const year = value.getFullYear();
    const month = padStart(String(value.getMonth() + 1), 2, '0');
    const dayOfMonth = padStart(String(value.getDate()), 2, '0');

    return cy.get(this.SELECTOR_BIRTHDAY_FIELD).clear().type(year + '-' + month + '-' + dayOfMonth);
  }

  writeBusinessPhone(value: string) {
    return cy.get(this.SELECTOR_BUSINESS_PHONE_FIELD).clear().type(value);
  }

  writePrivatePhone(value: string) {
    return cy.get(this.SELECTOR_PRIVATE_PHONE_FIELD).clear().type(value);
  }

  writeMobilePhone(value: string) {
    return cy.get(this.SELECTOR_MOBILE_PHONE_FIELD).clear().type(value);
  }

  writeCovid19Vaccinated(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_COVID_19_VACCINATED_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_COVID_19_VACCINATED_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeOnFirstNameBasis(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_ON_FIRST_NAME_BASIS_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_ON_FIRST_NAME_BASIS_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeGlasses(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_GLASSES_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_GLASSES_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeContacts(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_CONTACTS_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_CONTACTS_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeNeurodermatitis(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_NEURODERMATITIS_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_NEURODERMATITIS_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeClaustrophobia(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_CLAUSTROPHOBIA_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_CLAUSTROPHOBIA_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeDiabetes(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_DIABETES_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_DIABETES_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeEpilepsy(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_EPILEPSY_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_EPILEPSY_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeCirculationProblems(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_CIRCULATION_PROBLEMS_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_CIRCULATION_PROBLEMS_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeDryEyes(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_DRY_EYES_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_DRY_EYES_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeAsthma(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_ASTHMA_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_ASTHMA_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeCyanoacrylateAllergy(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_CYANOACRYLATE_ALLERGY_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_CYANOACRYLATE_ALLERGY_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writePlasterAllergy(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_PLASTER_ALLERGY_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_PLASTER_ALLERGY_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  writeHayFever(value: boolean) {
    if (value) {
      return cy.get(this.SELECTOR_HAY_FEVER_CHECKBOX).check({force: true}).should('be.checked')
    } else {
      return cy.get(this.SELECTOR_HAY_FEVER_CHECKBOX).uncheck({force: true}).should('not.be.checked')
    }
  }

  saveChanges(create: boolean) {
    cy.intercept({
      url: create ? '/backend/customers' : '/backend/customers/**',
      method: create ? 'POST' : 'PUT'
    }).as('modifyCustomerCall')
      .get(this.SELECTOR_CUSTOMER_FORM).submit().wait('@modifyCustomerCall');
  }

  close(): void {
    cy.get(this.SELECTOR_CUSTOMER_FORM).type('{esc}')
      .get(this.SELECTOR_CUSTOMER_FORM).should('not.exist');
  }

  validateFirstNameEquality(expected: string) {
    cy.get(this.SELECTOR_FIRSTNAME_FIELD).should('have.value', expected);
  }
  validateLastNameEquality(expected: string) {
    cy.get(this.SELECTOR_LASTNAME_FIELD).should('have.value', expected);
  }
  validateEmailEquality(expected: string) {
    cy.get(this.SELECTOR_EMAIL_FIELD).should('have.value', expected);
  }
  validateAddressEquality(expected: string) {
    cy.get(this.SELECTOR_ADDRESS_FIELD).should('have.value', expected);
  }
  validatePostalCodeEquality(expected: number) {
    cy.get(this.SELECTOR_POSTALCODE_FIELD).should('have.value', expected);
  }
  validateCityEquality(expected: string) {
    cy.get(this.SELECTOR_CITY_FIELD).should('have.value', expected);
  }

  validatePrivatePhoneEquality(expected: string) {
    cy.get(this.SELECTOR_PRIVATE_PHONE_FIELD).should('have.value', expected);
  }

  validateBusinessPhoneEquality(expected: string) {
    cy.get(this.SELECTOR_BUSINESS_PHONE_FIELD).should('have.value', expected);
  }

  validateMobilePhoneEquality(expected: string) {
    cy.get(this.SELECTOR_MOBILE_PHONE_FIELD).should('have.value', expected);
  }

  validateBirthdayEquality(expected: Date) {
    const year = expected.getFullYear();
    const month = padStart(String(expected.getMonth() + 1), 2, '0');
    const dayOfMonth = padStart(String(expected.getDate()), 2, '0');
    cy.get(this.SELECTOR_BIRTHDAY_FIELD).should('have.value', `${year}-${month}-${dayOfMonth}`);
  }

  validateOnFirstNameBasisEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_ON_FIRST_NAME_BASIS_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateHayFeverEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_HAY_FEVER_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validatePlasterAllergyEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_HAY_FEVER_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateCyanoacrylateAllergyEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_CYANOACRYLATE_ALLERGY_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateAsthmaEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_ASTHMA_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateDryEyesEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_DRY_EYES_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateCirculationProblemsEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_CIRCULATION_PROBLEMS_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateEpilepsyEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_EPILEPSY_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }
  validateDiabetesEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_DIABETES_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }
  validateClaustrophobiaEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_CLAUSTROPHOBIA_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }
  validateNeurodermatitisEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_NEURODERMATITIS_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }
  validateContactsEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_CONTACTS_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }
  validateGlassesEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_GLASSES_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }
  validateCovid19VaccinatedEquality(expected: boolean) {
    const chainable = cy.get(this.SELECTOR_COVID_19_VACCINATED_CHECKBOX);
    if (expected) {
      return chainable.should('be.checked')
    } else {
      return chainable.should('not.be.checked')
    }
  }

  validateAdditionalInfoEquality(expected: string) {
    cy.get(this.SELECTOR_ADDITIONAL_INFO_FIELD).should('have.value', expected);
  }
  validateEyeDiseaseEquality(expected: string) {
    cy.get(this.SELECTOR_EYE_DISEASE_FIELD).should('have.value', expected);
  }
  validateAllergyEquality(expected: string) {
    cy.get(this.SELECTOR_ALLERGY_FIELD).should('have.value', expected);
  }

  validateMedicalInfoEquality(expected: string) {
    cy.get(this.SELECTOR_MEDICAL_INFO_FIELD).should('have.value', expected);
  }

}
