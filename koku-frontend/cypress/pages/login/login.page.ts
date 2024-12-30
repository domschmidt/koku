import {KokuOpenablePublicPage} from "../../page-helper/types/koku-openable-public.page";
import {environment} from "../../environment.props";
import {WelcomeComponentPage} from "../welcome/welcome.page";

export class LoginPage implements KokuOpenablePublicPage {

  private readonly SELECTOR_LOGIN_FORM = '#login-form';
  private readonly SELECTOR_USERNAME_FIELD = '#login-form__username-input';
  private readonly SELECTOR_PASSWORD_FIELD = '#login-form__password-input';
  private readonly SELECTOR_SUBMIT_BUTTON = '#login-form__submit-btn';

  private readonly DEFAULT_USERNAME = 'admin';
  private readonly DEFAULT_PASSWORD = 'admin';

  validatePageOpened(): void {
    cy.get(this.SELECTOR_LOGIN_FORM).find(this.SELECTOR_USERNAME_FIELD)
      .get(this.SELECTOR_LOGIN_FORM).find(this.SELECTOR_PASSWORD_FIELD)
      .get(this.SELECTOR_LOGIN_FORM).find(this.SELECTOR_SUBMIT_BUTTON);
  }

  login(username: string, password: string) {
    cy.intercept({
      url: '/backend/auth/login',
      method: 'POST'
    }).as('loginCall');
    cy.get(this.SELECTOR_USERNAME_FIELD).clear().type(username)
      .get(this.SELECTOR_PASSWORD_FIELD).clear().type(password)
      .get(this.SELECTOR_SUBMIT_BUTTON).click().wait('@loginCall');
    const welcomePage = new WelcomeComponentPage();
    welcomePage.validatePageOpened();
    return welcomePage;
  }

  loginWithDefaultCredentials(): WelcomeComponentPage {
    return this.login(this.DEFAULT_USERNAME, this.DEFAULT_PASSWORD);
  }

  open(): void {
    cy.visit(environment.address + '/login');
    this.validatePageOpened();
  }
}
