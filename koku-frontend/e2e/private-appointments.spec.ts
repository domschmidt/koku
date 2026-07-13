import { APIResponse, Page } from '@playwright/test';
import { expect, test } from './fixtures/koku-test';
import { CreatedEntity } from './pages/form.page';

test.describe('private appointments', () => {
  test('creates a private appointment with an exact time range', async ({
    calendarPage,
    loginPage,
    page,
    privateAppointmentPage,
  }) => {
    await loginPage.signIn();
    await calendarPage.open();
    await calendarPage.openNewPrivateAppointment();
    await privateAppointmentPage.expectLoaded();

    let appointment: CreatedEntity | undefined;
    try {
      appointment = await privateAppointmentPage.create(`E2E Privat ${Date.now()}`);
    } finally {
      await cleanup(page, appointment);
    }
  });
});

async function cleanup(page: Page, entity: CreatedEntity | undefined): Promise<void> {
  if (!entity) return;
  const response = await page.request.delete(`/services/users/users/appointments/${entity.id}`, {
    headers: { Authorization: entity.authorization },
  });
  expectCleanupSuccessful(response);
}

function expectCleanupSuccessful(response: APIResponse): void {
  expect(response.ok(), `Cleanup failed with HTTP ${response.status()}.`).toBe(true);
}
