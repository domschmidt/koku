import {KokuNavigatablePage} from "../types/koku-navgatable-internal.page";
import {MyProfilePage} from "../../pages/user/my-profile.page";
import {LoginPage} from "../../pages/login/login.page";

export class KokuNavigation {

  navigateToPage<T extends KokuNavigatablePage>(
    navigationTarget: string,
    targetPageClazz: T
  ) {
    this.openNavigation();
    this.navigateTo(navigationTarget);
    targetPageClazz.validatePageOpened();
    return targetPageClazz;
  }

  navigateToMyProfile() {
    this.openNavigation();
    cy.intercept({
      url: '/backend/users/@self',
      method: 'GET'
    }).as('myProfileCall');
    cy.get('#page-skeleton__my-profile').click();
    const result = new MyProfilePage();
    cy.wait('@myProfileCall')
    result.validatePageOpened();
    return result;
  }

  logout() {
    this.openNavigation();
    cy.intercept({
      url: '/backend/auth/logout',
      method: 'POST'
    }).as('logoutCall');
    this.navigateTo('Logout').wait('@logoutCall');
    const loginComponentPage = new LoginPage();
    loginComponentPage.validatePageOpened();
    return loginComponentPage;
  }

  public openNavigation() {
    cy.get('#page-skeleton__open-navi-btn').click()
      .get('#page-skeleton').find('#page-skeleton__close-navi-btn');
  }

  private navigateTo(navigationTarget: string) {
    return cy.get('#navigation__top-section').get('a').contains(navigationTarget).click();
  }

}
