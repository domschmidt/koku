// from your cypress/integration/spec.ts
import {environment} from "../../environment.props";

it('works', () => {
    cy.visit(environment.address + '/login');

    cy.get('#login-form__username-input').type('admin');
    cy.get('#login-form__password-input').type('admin');
    cy.get('#login-form__submit-btn').click();

})
