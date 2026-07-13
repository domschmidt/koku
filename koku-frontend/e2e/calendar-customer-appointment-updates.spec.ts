import { Page } from '@playwright/test';
import { expect, test } from './fixtures/koku-test';
import { CreatedEntity } from './pages/form.page';

test.describe('calendar customer appointment updates', () => {
  test.beforeEach(async ({ calendarPage, customerAppointmentPage, loginPage }) => {
    await loginPage.signIn();
    await calendarPage.open();
    await calendarPage.openNewAppointment();
    await customerAppointmentPage.expectLoaded();
  });

  test('adds a customer appointment to the calendar', async ({ calendarPage, customerAppointmentPage, page }) => {
    const entities = await createAppointment(customerAppointmentPage);
    try {
      await calendarPage.expectAppointmentVisible(entities.appointment.id);
    } finally {
      await cleanupEntities(page, entities);
    }
  });

  test('moves a customer appointment to another day', async ({ calendarPage, customerAppointmentPage, page }) => {
    const entities = await createAppointment(customerAppointmentPage);
    try {
      await calendarPage.expectAppointmentVisible(entities.appointment.id);
      await calendarPage.moveAppointment(entities.appointment.id, '21. Juli 2026');
      await calendarPage.expectAppointmentVisible(entities.appointment.id);
    } finally {
      await cleanupEntities(page, entities);
    }
  });

  test('deletes a customer appointment from its calendar detail', async ({
    calendarPage,
    customerAppointmentPage,
    page,
  }) => {
    const entities = await createAppointment(customerAppointmentPage);
    try {
      await calendarPage.openAppointment(entities.appointment.id);
      await customerAppointmentPage.expectLoaded();
      await customerAppointmentPage.deleteCurrentAppointment();
      await expect(page.getByTestId(`calendar-event-customer-appointments-${entities.appointment.id}`)).toBeHidden();
    } finally {
      await cleanupEntities(page, entities, true);
    }
  });

  test('keeps a customer appointment when deletion is cancelled', async ({
    calendarPage,
    customerAppointmentPage,
    page,
  }) => {
    const entities = await createAppointment(customerAppointmentPage);
    try {
      await calendarPage.openAppointment(entities.appointment.id);
      await customerAppointmentPage.expectLoaded();
      await page.getByTestId('customer-appointment-form').getByTitle('Jetzt löschen').click();
      const confirmation = page.getByRole('dialog').filter({ hasText: 'Kundentermin löschen' });
      await confirmation.getByRole('button', { name: 'Abbrechen', exact: true }).click();
      await expect(confirmation).toBeHidden();
      await expect(page.getByTestId('customer-appointment-form')).toBeVisible();
    } finally {
      await cleanupEntities(page, entities);
    }
  });
});

async function createAppointment(
  customerAppointmentPage: import('./pages/customer-appointment.page').CustomerAppointmentPage,
) {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const customerName = `E2E Kalender-${suffix}`;
  const customer = await customerAppointmentPage.createCustomer('E2E', `Kalender-${suffix}`);
  const appointment = await customerAppointmentPage.createAppointment('20.07.2026', '10:15', customerName);
  return { appointment, customer };
}

async function cleanupEntities(
  page: Page,
  entities: { appointment: CreatedEntity; customer: CreatedEntity },
  appointmentAlreadyDeleted = false,
): Promise<void> {
  if (!appointmentAlreadyDeleted) {
    await cleanup(page, '/services/customers/customers/appointments', entities.appointment);
  }
  await cleanup(page, '/services/customers/customers', entities.customer);
}

async function cleanup(page: Page, endpoint: string, entity: CreatedEntity): Promise<void> {
  const response = await page.request.delete(`${endpoint}/${entity.id}`, {
    headers: { Authorization: entity.authorization },
  });
  expect(response.ok(), `Cleanup failed with HTTP ${response.status()}.`).toBe(true);
}
