import { APIResponse, Page } from '@playwright/test';
import { CreatedEntity } from './pages/customer-appointment.page';
import { expect, test } from './fixtures/koku-test';

test.describe('customer appointments', () => {
  test.beforeEach(async ({ calendarPage, customerAppointmentPage, loginPage }) => {
    await loginPage.signIn();
    await calendarPage.open();
    await calendarPage.openNewAppointment();
    await customerAppointmentPage.expectLoaded();
  });

  test('creates and selects a customer inside a new appointment', async ({ customerAppointmentPage, page }) => {
    const suffix = uniqueSuffix();
    let customer: CreatedEntity | undefined;
    try {
      customer = await customerAppointmentPage.createCustomer('E2E', `Kunde-${suffix}`);
    } finally {
      await cleanup(page, '/services/customers/customers', customer);
    }
  });

  test('creates and selects an activity inside a new appointment', async ({ customerAppointmentPage, page }) => {
    let activity: CreatedEntity | undefined;
    try {
      activity = await customerAppointmentPage.createActivity(`E2E Tätigkeit ${uniqueSuffix()}`, '00:45', '42.50');
    } finally {
      await cleanup(page, '/services/activities/activities', activity);
    }
  });

  test('creates and selects a product inside a new appointment', async ({ customerAppointmentPage, page }) => {
    let product: CreatedEntity | undefined;
    try {
      product = await customerAppointmentPage.createProduct(`E2E Produkt ${uniqueSuffix()}`, '19.95');
    } finally {
      await cleanup(page, '/services/products/products', product);
    }
  });

  test('calculates the expected appointment duration from the selected activity', async ({
    customerAppointmentPage,
  }) => {
    await customerAppointmentPage.selectFirstActivityAndExpectDuration();
  });
});

async function cleanup(page: Page, endpoint: string, entity: CreatedEntity | undefined): Promise<void> {
  if (!entity) {
    return;
  }

  const response = await page.request.delete(`${endpoint}/${entity.id}`, {
    headers: { Authorization: entity.authorization },
  });
  expectCleanupSuccessful(response);
}

function expectCleanupSuccessful(response: APIResponse): void {
  expect(response.ok(), `Cleanup failed with HTTP ${response.status()}.`).toBe(true);
}

function uniqueSuffix(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
