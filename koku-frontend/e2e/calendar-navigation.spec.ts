import { test } from './fixtures/koku-test';

test.describe('calendar navigation', () => {
  test.beforeEach(async ({ calendarPage, loginPage }) => {
    await loginPage.signIn();
    await calendarPage.open();
  });

  test('switches between month, week and day views', async ({ calendarPage }) => {
    await calendarPage.selectView('Woche');
    await calendarPage.selectView('Tag');
    await calendarPage.selectView('Monat');
  });

  test('navigates between months', async ({ calendarPage }) => {
    await calendarPage.selectView('Monat');
    await calendarPage.expectPeriodChanges('Monat', 'Weiter');
    await calendarPage.expectPeriodChanges('Monat', 'Zurück');
  });

  test('navigates between weeks', async ({ calendarPage }) => {
    await calendarPage.selectView('Woche');
    await calendarPage.expectPeriodChanges('Woche', 'Weiter');
    await calendarPage.expectPeriodChanges('Woche', 'Zurück');
  });

  test('navigates between days', async ({ calendarPage }) => {
    await calendarPage.selectView('Tag');
    await calendarPage.expectPeriodChanges('Tag', 'Weiter');
    await calendarPage.expectPeriodChanges('Tag', 'Zurück');
  });
});
