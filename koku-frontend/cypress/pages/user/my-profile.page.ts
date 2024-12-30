import {KokuNavigation} from "../../page-helper/navigation/koku.navigation";

export class MyProfilePage {

  private readonly SELECTOR_MY_PROFILE_FORM = '#my-profile-form';
  private readonly SELECTOR_USERNAME_FIELD = '#user-profile__username';
  private readonly SELECTOR_PASSWORD_FIELD = '#user-profile__password';
  private readonly SELECTOR_FIRSTNAME_FIELD = '#user-profile__firstname';
  private readonly SELECTOR_LASTNAME_FIELD = '#user-profile__lastname';

  validatePageOpened(): void {
    cy.get(this.SELECTOR_MY_PROFILE_FORM).find(this.SELECTOR_USERNAME_FIELD)
      .get(this.SELECTOR_MY_PROFILE_FORM).find(this.SELECTOR_PASSWORD_FIELD)
      .get(this.SELECTOR_MY_PROFILE_FORM).find(this.SELECTOR_FIRSTNAME_FIELD)
      .get(this.SELECTOR_MY_PROFILE_FORM).find(this.SELECTOR_LASTNAME_FIELD);
  }

  writeUserDetails(username: string, firstname: string, lastname: string, password: string) {
    cy.get(this.SELECTOR_USERNAME_FIELD).clear().type(username)
      .get(this.SELECTOR_FIRSTNAME_FIELD).clear().type(firstname)
      .get(this.SELECTOR_LASTNAME_FIELD).clear().type(lastname)
      .get(this.SELECTOR_PASSWORD_FIELD).clear().type(password)
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

  saveChanges() {
    cy.intercept({
      url: '/backend/users/@self',
      method: 'PUT'
    }).as('updateMyProfileCall')
      .get(this.SELECTOR_MY_PROFILE_FORM).submit().wait('@updateMyProfileCall');
  }

  getNavigation(): KokuNavigation {
    return new KokuNavigation()
  }

}
