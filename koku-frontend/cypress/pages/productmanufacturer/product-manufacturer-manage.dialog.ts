import {KokuDialog} from "../../page-helper/dialogs/koku.dialog";

export class ProductManufacturerManageDialog implements KokuDialog {
  private readonly SELECTOR_PRODUCT_MANUFACTURER_FORM = '#product-manufacturer-details-form';
  private readonly SELECTOR_NAME_FIELD = '#product-manufacturer-details-form__name';

  validateDialogOpened(): void {
    cy.get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).find(this.SELECTOR_NAME_FIELD);
  }

  close(): void {
    cy.get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).type('{esc}')
      .get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).should('not.exist');
  }

  writeName(value: string) {
    cy.get(this.SELECTOR_NAME_FIELD).clear().type(value);
  }

  validateName(expected: string) {
    cy.get(this.SELECTOR_NAME_FIELD).should('have.value', expected);
  }

  saveChanges(create: boolean) {
    cy.intercept({
      url: create ? '/backend/productmanufacturers' : '/backend/productmanufacturers/**',
      method: create ? 'POST' : 'PUT'
    }).as('modifyProductManufacturerCall')
      .get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).submit().wait('@modifyProductManufacturerCall')
      .get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).should('not.exist');
  }


}
