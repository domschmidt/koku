import {KokuDialog} from "../../page-helper/dialogs/koku.dialog";
import {UserAdministrationPage} from "./user-administration.page";

export class UserManageDialog implements KokuDialog {

  private readonly SELECTOR_USER_FORM = '#user-manage-form';
  private readonly SELECTOR_USERNAME_FIELD = '#user-profile__username';
  private readonly SELECTOR_PASSWORD_FIELD = '#user-profile__password';
  private readonly SELECTOR_FIRSTNAME_FIELD = '#user-profile__firstname';
  private readonly SELECTOR_LASTNAME_FIELD = '#user-profile__lastname';

  validateDialogOpened(): void {
    cy.get(this.SELECTOR_USER_FORM).find(this.SELECTOR_USERNAME_FIELD)
      .get(this.SELECTOR_USER_FORM).find(this.SELECTOR_PASSWORD_FIELD)
      .get(this.SELECTOR_USER_FORM).find(this.SELECTOR_FIRSTNAME_FIELD)
      .get(this.SELECTOR_USER_FORM).find(this.SELECTOR_LASTNAME_FIELD);
  }

  writeUserDetails(username: string, firstname: string, lastname: string, password: string) {
    cy.get(this.SELECTOR_USERNAME_FIELD).clear().type(username)
      .get(this.SELECTOR_FIRSTNAME_FIELD).clear().type(firstname)
      .get(this.SELECTOR_LASTNAME_FIELD).clear().type(lastname)
      .get(this.SELECTOR_PASSWORD_FIELD).clear().type(password);
  }

  saveChanges(create: boolean) {
    cy.intercept({
      url: create ? '/backend/users' : '/backend/users/**',
      method: create ? 'POST' : 'PUT'
    }).as('userUpdateCall')
      .get(this.SELECTOR_USER_FORM)
      .submit()
      .wait('@userUpdateCall');
    const userAdministrationPage = new UserAdministrationPage();
    userAdministrationPage.validatePageOpened();
    return userAdministrationPage;
  }

  validateUsernameEquality(expected: string) {
    cy.get(this.SELECTOR_USERNAME_FIELD).should('have.value', expected);
  }

  validateFirstnameEquality(expected: string) {
    cy.get(this.SELECTOR_FIRSTNAME_FIELD).should('have.value', expected);
  }

  validateLastnameEquality(expected: string) {
    cy.get(this.SELECTOR_LASTNAME_FIELD).should('have.value', expected);
  }

  close(): void {
    cy.get(this.SELECTOR_USER_FORM).type('{esc}')
      .get(this.SELECTOR_USER_FORM).should('not.exist');
  }
}
