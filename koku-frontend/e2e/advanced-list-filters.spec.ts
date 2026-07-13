import { expect, test } from './fixtures/koku-test';

test.describe('advanced list filters', () => {
  test.beforeEach(async ({ customersPage, loginPage }) => {
    await loginPage.signIn();
    await customersPage.open();
  });

  test('opens and closes the advanced customer filters', async ({ customersPage, page }) => {
    await customersPage.openAdvancedFilters();
    await expect(page.getByRole('checkbox', { name: 'Gelöschte anzeigen?', exact: true })).toBeVisible();
    await page.getByRole('button', { name: 'Erweiterte Filter' }).click();
    await expect(page.getByTestId('list-advanced-filters')).toBeHidden();
  });

  test('cycles the deleted customer tri-state filter and reloads the list', async ({ customersPage, page }) => {
    await customersPage.openAdvancedFilters();
    const filter = page.getByRole('checkbox', { name: 'Gelöschte anzeigen?', exact: true });
    await customersPage.cycleToggleFilter('Gelöschte anzeigen?');
    await expect(filter).toHaveJSProperty('indeterminate', true);
    await customersPage.cycleToggleFilter('Gelöschte anzeigen?');
    await expect(filter).toBeChecked();
    await customersPage.cycleToggleFilter('Gelöschte anzeigen?');
    await expect(filter).not.toBeChecked();
  });
});
