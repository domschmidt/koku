import {UserManageDialog} from "./user-manage.dialog";
import {KokuNavigatablePage} from "../../page-helper/types/koku-navgatable-internal.page";
import {KokuInternalPage} from "../../page-helper/types/koku-internal.page";
import {KokuNavigation} from "../../page-helper/navigation/koku.navigation";
import {KokuSearchPage} from "../../page-helper/types/koku-search.page";
import {KokuPageSearch} from "../../page-helper/navigation/koku.page-search";


export class UserAdministrationPage implements KokuSearchPage, KokuNavigatablePage, KokuInternalPage {

  private readonly SELECTOR_USER_PAGE = '#user-page';

  private readonly SELECTOR_USER_RESULTS = '#user-page__results';
  private readonly SELECTOR_SEARCH_FIELD = '#user-page__search';
  private readonly SELECTOR_ADD_NEW_USER_BUTTON = '#user-page__add-new-user-btn';

  validatePageOpened(): void {
    cy.get(this.SELECTOR_USER_PAGE).find(this.SELECTOR_SEARCH_FIELD);
  }

  openCreateDialog(): UserManageDialog {
    cy.get(this.SELECTOR_ADD_NEW_USER_BUTTON).click();
    const userManageDialog = new UserManageDialog();
    userManageDialog.validateDialogOpened();
    return userManageDialog;
  }

  getNavigation(): KokuNavigation {
    return new KokuNavigation();
  }

  getPageSearch(): KokuPageSearch {
    return new KokuPageSearch(
      '/api/users',
      '/api/users/**',
      this.SELECTOR_SEARCH_FIELD, this.SELECTOR_USER_RESULTS);
  }

}
