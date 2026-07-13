import { Page } from '@playwright/test';
import { expect, test } from './fixtures/koku-test';
import { CreatedEntity } from './pages/form.page';

test.describe('activity CRUD synchronization', () => {
  test.beforeEach(async ({ calendarPage, customerAppointmentPage, loginPage }) => {
    await loginPage.signIn();
    await calendarPage.open();
    await calendarPage.openNewAppointment();
    await customerAppointmentPage.expectLoaded();
  });

  test('deletes and restores an activity directly in the list', async ({
    activitiesPage,
    customerAppointmentPage,
    page,
  }) => {
    const { entity, name } = await createActivity(customerAppointmentPage);
    try {
      await page.goto('/manage/activities');
      await activitiesPage.expectLoaded();
      await activitiesPage.searchFor(name);
      await activitiesPage.deleteFromList(name);
      await activitiesPage.restoreFromList(name);
    } finally {
      await cleanupActivity(page, entity);
    }
  });

  test('synchronizes renamed and deleted state between detail, header and list', async ({
    activitiesPage,
    customerAppointmentPage,
    page,
  }) => {
    const { entity, name } = await createActivity(customerAppointmentPage);
    const renamed = `${name}-renamed`;
    try {
      await page.goto('/manage/activities');
      await activitiesPage.expectLoaded();
      await activitiesPage.openNamedActivity(name);
      await activitiesPage.renameOpenedActivity(renamed);
      await activitiesPage.deleteOpenedActivity();
      await activitiesPage.restoreOpenedActivity();
    } finally {
      await cleanupActivity(page, entity);
    }
  });
});

async function createActivity(
  customerAppointmentPage: import('./pages/customer-appointment.page').CustomerAppointmentPage,
) {
  const name = `E2E CRUD Activity-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const entity = await customerAppointmentPage.createActivity(name, '00:30', '25');
  return { entity, name };
}

async function cleanupActivity(page: Page, entity: CreatedEntity): Promise<void> {
  const response = await page.request.delete(`/services/activities/activities/${entity.id}`, {
    headers: { Authorization: entity.authorization },
  });
  expect(response.ok(), `Cleanup failed with HTTP ${response.status()}.`).toBe(true);
}
