import { test } from './fixtures/koku-test';

test.describe('product manufacturers', () => {
  test.beforeEach(async ({ loginPage, productManufacturersPage }) => {
    await loginPage.signIn();
    await productManufacturersPage.open();
  });
  test('shows product manufacturers', async ({ productManufacturersPage }) => {
    await productManufacturersPage.expectLoaded();
    await productManufacturersPage.expectResultsVisible();
  });
  test('filters and restores product manufacturers', async ({ productManufacturersPage }) => {
    await productManufacturersPage.searchFor('__koku_e2e_no_manufacturer__');
    await productManufacturersPage.expectNoResults();
    await productManufacturersPage.clearSearch();
    await productManufacturersPage.expectResultsVisible();
  });
  test('opens a product manufacturer detail', async ({ productManufacturersPage }) => {
    await productManufacturersPage.openFirstResult();
  });
});
