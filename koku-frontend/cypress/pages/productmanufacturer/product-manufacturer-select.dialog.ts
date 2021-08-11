import {KokuDialog} from "../../page-helper/dialogs/koku.dialog";
import {KokuSearchPage} from "../../page-helper/types/koku-search.page";
import {KokuPageSearch} from "../../page-helper/navigation/koku.page-search";
import {ProductManufacturerManageDialog} from "./product-manufacturer-manage.dialog";

export class ProductManufacturerSelectDialog implements KokuDialog, KokuSearchPage {

  private readonly SELECTOR_PRODUCT_MANUFACTURER_WRAPPER = '#product-manufacturer';
  private readonly SELECTOR_PRODUCT_MANUFACTURER_FORM = '#product-manufacturer__search-form';
  private readonly SELECTOR_SEARCH_FIELD = '#product-manufacturer__search-form__search';
  private readonly SELECTOR_PRODUCT_MANUFACTURER_RESULTS = '#product-manufacturer__search-form__results';
  private readonly SELECTOR_ADD_NEW_PRODUCT_MANUFACTURER_BUTTON = '#product-manufacturer__add-new-product-manufacturer-button';

  validateDialogOpened(): void {
    cy.get(this.SELECTOR_PRODUCT_MANUFACTURER_WRAPPER).find(this.SELECTOR_SEARCH_FIELD)
      .get(this.SELECTOR_PRODUCT_MANUFACTURER_WRAPPER).find(this.SELECTOR_PRODUCT_MANUFACTURER_FORM)
      .get(this.SELECTOR_PRODUCT_MANUFACTURER_WRAPPER).find(this.SELECTOR_ADD_NEW_PRODUCT_MANUFACTURER_BUTTON);
  }

  close(): void {
    cy.get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).type('{esc}');
    this.waitUntilClosed();
  }

  getPageSearch(): KokuPageSearch {
    return new KokuPageSearch(
      '/api/productmanufacturers',
      '/api/productmanufacturers/**',
      this.SELECTOR_SEARCH_FIELD,
      this.SELECTOR_PRODUCT_MANUFACTURER_RESULTS
    );
  }

  openCreateDialog() {
    cy.get(this.SELECTOR_ADD_NEW_PRODUCT_MANUFACTURER_BUTTON).click();
    const result = new ProductManufacturerManageDialog();
    result.validateDialogOpened();
    return result;
  }

  waitUntilClosed() {
    return cy.get(this.SELECTOR_PRODUCT_MANUFACTURER_FORM).should('not.exist');
  }
}
