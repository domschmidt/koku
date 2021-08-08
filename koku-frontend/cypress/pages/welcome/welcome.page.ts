import {KokuInternalPage} from "../../page-helper/types/koku-internal.page";
import {KokuNavigatablePage} from "../../page-helper/types/koku-navgatable-internal.page";
import {KokuNavigation} from "../../page-helper/navigation/koku.navigation";

export class WelcomeComponentPage implements KokuInternalPage, KokuNavigatablePage {

  validatePageOpened(): void {
    cy.get('#welcome-page').find('#welcome-page__welcome-message');
  }

  getNavigation(): KokuNavigation {
    return new KokuNavigation();
  }

}
