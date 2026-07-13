import { test } from './fixtures/koku-test';

test.describe('activity steps', () => {
  test.beforeEach(async ({ activityStepsPage, loginPage }) => {
    await loginPage.signIn();
    await activityStepsPage.open();
  });
  test('shows activity steps', async ({ activityStepsPage }) => {
    await activityStepsPage.expectLoaded();
    await activityStepsPage.expectResultsVisible();
  });
  test('filters and restores activity steps', async ({ activityStepsPage }) => {
    await activityStepsPage.searchFor('__koku_e2e_no_activity_step__');
    await activityStepsPage.expectNoResults();
    await activityStepsPage.clearSearch();
    await activityStepsPage.expectResultsVisible();
  });
  test('opens an activity step detail', async ({ activityStepsPage }) => {
    await activityStepsPage.openFirstResult();
  });
});
