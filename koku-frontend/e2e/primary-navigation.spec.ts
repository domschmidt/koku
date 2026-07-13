import { test } from './fixtures/koku-test';

test.describe('primary navigation', () => {
  test.beforeEach(async ({ loginPage }) => {
    await loginPage.signIn();
  });

  test('opens the calendar', async ({ calendarPage }) => {
    await calendarPage.open();
    await calendarPage.expectLoaded();
  });

  test('opens users', async ({ usersPage }) => {
    await usersPage.open();
    await usersPage.expectLoaded();
  });

  test('opens statistics', async ({ statisticsPage }) => {
    await statisticsPage.open();
    await statisticsPage.expectDashboard();
  });

  test('opens the profile and exposes logout', async ({ profilePage }) => {
    await profilePage.open();
    await profilePage.expectLoaded();
    await profilePage.expectLogoutReachable();
  });
});
