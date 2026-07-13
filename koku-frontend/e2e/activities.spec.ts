import { test } from './fixtures/koku-test';

test.describe('activities', () => {
  test.beforeEach(async ({ activitiesPage, loginPage }) => {
    await loginPage.signIn();
    await activitiesPage.open();
  });
  test('shows activities', async ({ activitiesPage }) => {
    await activitiesPage.expectLoaded();
    await activitiesPage.expectResultsVisible();
  });
  test('filters and restores activities', async ({ activitiesPage }) => {
    await activitiesPage.searchFor('__koku_e2e_no_activity__');
    await activitiesPage.expectNoResults();
    await activitiesPage.clearSearch();
    await activitiesPage.expectResultsVisible();
  });
  test('opens an activity detail', async ({ activitiesPage }) => {
    await activitiesPage.openFirstResult();
  });
});
