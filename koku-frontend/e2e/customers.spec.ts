import { test } from './fixtures/koku-test';

test.describe('customers', () => {
  test.beforeEach(async ({ loginPage, customersPage }) => {
    await loginPage.signIn();
    await customersPage.open();
  });

  test('shows the customer list', async ({ customersPage }) => {
    await customersPage.expectLoaded();
    await customersPage.expectResultsVisible();
  });

  test('filters customers and restores the complete list', async ({ customersPage }) => {
    await customersPage.expectResultsVisible();

    await customersPage.searchFor('__koku_e2e_customer_without_match__');
    await customersPage.expectNoResults();

    await customersPage.clearSearch();
    await customersPage.expectResultsVisible();
  });

  test('opens an existing customer detail', async ({ customersPage }) => {
    await customersPage.openFirstResult();
  });
});
