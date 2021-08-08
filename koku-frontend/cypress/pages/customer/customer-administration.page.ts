import {KokuTabPage} from "../../page-helper/types/koku-tab.page";
import {KokuNavigatablePage} from "../../page-helper/types/koku-navgatable-internal.page";
import {KokuInternalPage} from "../../page-helper/types/koku-internal.page";
import {KokuNavigation} from "../../page-helper/navigation/koku.navigation";
import {KokuTabBar} from "../../page-helper/navigation/koku.tab-bar";
import {KokuPageSearch} from "../../page-helper/navigation/koku.page-search";
import {KokuSearchPage} from "../../page-helper/types/koku-search.page";
import {CustomerManageDialog} from "./customer-manage.dialog";

export class CustomerAdministrationPage implements KokuTabPage, KokuNavigatablePage, KokuInternalPage, KokuSearchPage {

  private readonly SELECTOR_CUSTOMER_PAGE = "#customer-page"
  private readonly SELECTOR_USER_RESULTS = '#customer-page__results';
  private readonly SELECTOR_SEARCH_FIELD = '#customer-page__search';
  private readonly SELECTOR_ADD_NEW_USER_BUTTON = '#customer-page__add-new-customer-btn';

  validatePageOpened(): void {
    cy.get(this.SELECTOR_CUSTOMER_PAGE).find(this.SELECTOR_USER_RESULTS)
      .get(this.SELECTOR_CUSTOMER_PAGE).find(this.SELECTOR_SEARCH_FIELD);
  }

  openCreateDialog(): CustomerManageDialog {
    cy.get(this.SELECTOR_ADD_NEW_USER_BUTTON).click();
    const customerManageDialog = new CustomerManageDialog();
    customerManageDialog.validateDialogOpened();
    return customerManageDialog;
  }

  getNavigation(): KokuNavigation {
    return new KokuNavigation();
  }

  getTabBar(): KokuTabBar {
    return new KokuTabBar();
  }

  getPageSearch(): KokuPageSearch {
    return new KokuPageSearch(
      '/api/customers',
      '/api/customers/**',
      this.SELECTOR_SEARCH_FIELD,
      this.SELECTOR_USER_RESULTS
    );
  }

}
