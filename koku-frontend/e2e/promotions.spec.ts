import { test } from './fixtures/koku-test';

test.describe('promotions', () => {
  test.beforeEach(async ({ loginPage, promotionsPage }) => {
    await loginPage.signIn();
    await promotionsPage.open();
  });
  test('shows promotions', async ({ promotionsPage }) => {
    await promotionsPage.expectLoaded();
    await promotionsPage.expectResultsVisible();
  });
  test('filters and restores promotions', async ({ promotionsPage }) => {
    await promotionsPage.searchFor('__koku_e2e_no_promotion__');
    await promotionsPage.expectNoResults();
    await promotionsPage.clearSearch();
    await promotionsPage.expectResultsVisible();
  });
  test('opens a promotion detail', async ({ promotionsPage }) => {
    await promotionsPage.openFirstResult();
  });
});
