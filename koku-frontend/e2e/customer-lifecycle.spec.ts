import { Page } from '@playwright/test';
import { expect, test } from './fixtures/koku-test';
import { CreatedEntity } from './pages/form.page';

test.describe('customer lifecycle', () => {
  test('soft-deletes and restores an isolated customer', async ({
    calendarPage,
    customerAppointmentPage,
    customersPage,
    loginPage,
    page,
  }) => {
    await loginPage.signIn();
    await calendarPage.open();
    await calendarPage.openNewAppointment();
    const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    const customerName = `E2E Lifecycle-${suffix}`;
    const customer = await customerAppointmentPage.createCustomer('E2E', `Lifecycle-${suffix}`);

    try {
      await page.goto('/manage/customers');
      await customersPage.expectLoaded();
      await customersPage.openNamedCustomer(customerName);
      await customersPage.renameOpenedCustomer('E2E Updated', `Lifecycle-${suffix}`);
      await customersPage.deleteOpenedCustomer();
      await customersPage.restoreOpenedCustomer();
    } finally {
      await cleanupCustomer(page, customer);
    }
  });

  test('combines advanced deleted-state and global-search predicates', async ({ customersPage, loginPage, page }) => {
    await loginPage.signIn();
    await customersPage.open();
    await customersPage.openAdvancedFilters();
    await customersPage.cycleToggleFilter('Gelöschte anzeigen?');
    await customersPage.expectCustomerHidden('__koku_e2e_deleted_customer_without_match__');
    await expect(page.getByRole('checkbox', { name: 'Gelöschte anzeigen?', exact: true })).toHaveJSProperty(
      'indeterminate',
      true,
    );
  });
});

async function cleanupCustomer(page: Page, customer: CreatedEntity): Promise<void> {
  const response = await page.request.delete(`/services/customers/customers/${customer.id}`, {
    headers: { Authorization: customer.authorization },
  });
  expect(response.ok(), `Cleanup failed with HTTP ${response.status()}.`).toBe(true);
}
