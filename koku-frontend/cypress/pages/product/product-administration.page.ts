import {KokuTabPage} from "../../page-helper/types/koku-tab.page";
import {KokuNavigatablePage} from "../../page-helper/types/koku-navgatable-internal.page";
import {KokuInternalPage} from "../../page-helper/types/koku-internal.page";
import {KokuNavigation} from "../../page-helper/navigation/koku.navigation";
import {KokuTabBar} from "../../page-helper/navigation/koku.tab-bar";
import {KokuPageSearch} from "../../page-helper/navigation/koku.page-search";
import {KokuSearchPage} from "../../page-helper/types/koku-search.page";
import {ProductManageDialog} from "./product-manage.dialog";

export class ProductAdministrationPage implements KokuTabPage, KokuNavigatablePage, KokuInternalPage, KokuSearchPage {

  private readonly SELECTOR_PRODUCT_PAGE = "#product-page"
  private readonly SELECTOR_PRODUCT_RESULTS = '#product-page__results';
  private readonly SELECTOR_SEARCH_FIELD = '#product-page__search';
  private readonly SELECTOR_ADD_NEW_PRODUCT_BUTTON = '#product-page__add-new-product-btn';

  validatePageOpened(): void {
    cy.get(this.SELECTOR_PRODUCT_PAGE).find(this.SELECTOR_SEARCH_FIELD)
      .get(this.SELECTOR_PRODUCT_PAGE).find(this.SELECTOR_ADD_NEW_PRODUCT_BUTTON);
  }

  openCreateDialog(): ProductManageDialog {
    cy.get(this.SELECTOR_ADD_NEW_PRODUCT_BUTTON).click();
    const productManageDialog = new ProductManageDialog();
    productManageDialog.validateDialogOpened();
    return productManageDialog;
  }

  getNavigation(): KokuNavigation {
    return new KokuNavigation();
  }

  getTabBar(): KokuTabBar {
    return new KokuTabBar();
  }

  getPageSearch(): KokuPageSearch {
    return new KokuPageSearch(
      '/api/products',
      '/api/products/**',
      this.SELECTOR_SEARCH_FIELD,
      this.SELECTOR_PRODUCT_RESULTS
    );
  }

}
