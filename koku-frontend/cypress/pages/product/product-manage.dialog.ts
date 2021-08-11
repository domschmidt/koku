import {KokuDialog} from "../../page-helper/dialogs/koku.dialog";
import {ProductManufacturerSelectDialog} from "../productmanufacturer/product-manufacturer-select.dialog";

export class ProductManageDialog implements KokuDialog {

  private readonly SELECTOR_PRODUCT_FORM = '#product-details-form';

  private readonly SELECTOR_DESCRIPTION_FIELD = '#product-details-form__description';
  private readonly SELECTOR_PRICE_FIELD = '#product-details-form__price';
  private readonly SELECTOR_PRICE_CHART = '#product-details-form__price-chart';
  private readonly SELECTOR_MANUFACTURER_BUTTON = '.product-details-form__manufacturer';

  validateDialogOpened(): void {
    cy.get(this.SELECTOR_PRODUCT_FORM).find(this.SELECTOR_DESCRIPTION_FIELD)
      .get(this.SELECTOR_PRODUCT_FORM).find(this.SELECTOR_PRICE_FIELD)
      .get(this.SELECTOR_PRODUCT_FORM).find(this.SELECTOR_MANUFACTURER_BUTTON);
  }

  writeDescription(value: string) {
    return cy.get(this.SELECTOR_DESCRIPTION_FIELD).clear().type(value);
  }

  writePrice(value: string) {
    return cy.get(this.SELECTOR_PRICE_FIELD).clear().type(value);
  }

  openManufacturerSelection() {
    cy.get(this.SELECTOR_MANUFACTURER_BUTTON).click();
    const result = new ProductManufacturerSelectDialog();
    result.validateDialogOpened();
    return result;
  }

  validateDescription(expected: string) {
    cy.get(this.SELECTOR_DESCRIPTION_FIELD).should('have.value', expected);
  }

  validatePrice(expected: string) {
    cy.get(this.SELECTOR_PRICE_FIELD).should('have.value', expected);
  }

  saveChanges(create: boolean) {
    cy.intercept({
      url: create ? '/api/products' : '/api/products/**',
      method: create ? 'POST' : 'PUT'
    }).as('modifyProductCall')
      .get(this.SELECTOR_PRODUCT_FORM).submit().wait('@modifyProductCall')
      .get(this.SELECTOR_PRODUCT_FORM).should('not.exist');
  }

  close(): void {
    cy.get(this.SELECTOR_PRODUCT_FORM).type('{esc}')
      .get(this.SELECTOR_PRODUCT_FORM).should('not.exist');
  }

}
