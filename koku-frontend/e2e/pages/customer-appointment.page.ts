import { expect, Page } from '@playwright/test';
import { CreatedEntity, FormPage } from './form.page';

export { CreatedEntity } from './form.page';

export class CustomerAppointmentPage extends FormPage {
  constructor(page: Page) {
    super(
      page,
      page.getByTestId('customer-appointment-form').filter({
        has: page.getByPlaceholder('Weitere Tätigkeiten...'),
      }),
    );
  }

  async createCustomer(firstName: string, lastName: string): Promise<CreatedEntity> {
    const dialog = await this.openNestedForm('Neuer Kunde anlegen', '/customers/customers/form', 'customer-form');
    await dialog.getByLabel('Vorname', { exact: true }).fill(firstName);
    const lastNameInput = dialog.getByLabel('Nachname', { exact: true });
    await lastNameInput.fill(lastName);
    await lastNameInput.press('Tab');

    const entity = await this.submitNestedForm(
      dialog,
      '/customers/customers',
      `${firstName} ${lastName}`,
      ['id', 'firstName', 'lastName'],
    );
    await expect(this.form.getByLabel('Kunde', { exact: true })).toHaveValue(`${firstName} ${lastName}`);
    return entity;
  }

  async createActivity(name: string, duration: string, price: string): Promise<CreatedEntity> {
    const dialog = await this.openNestedForm('Neue Tätigkeit anlegen', '/activities/activities/form', 'activity-form');
    await dialog.getByLabel('Name', { exact: true }).fill(name);
    await dialog.getByLabel('Ungefähre Behandlungsdauer', { exact: true }).fill(duration);
    await dialog.getByLabel('Preis', { exact: true }).fill(price);

    const entity = await this.submitNestedForm(dialog, '/activities/activities', name, ['id', 'name']);
    await expect(this.form.getByText(name)).toBeVisible();
    return entity;
  }

  async createProduct(name: string, price: string): Promise<CreatedEntity> {
    const dialog = await this.openNestedForm('Neues Produkt anlegen', '/products/products/form', 'product-form');
    const manufacturer = dialog.getByLabel('Hersteller', { exact: true });
    await manufacturer.fill('a');
    await manufacturer.press('ArrowDown');
    await manufacturer.press('Enter');
    await dialog.getByLabel('Name', { exact: true }).fill(name);
    await dialog.getByLabel('Preis', { exact: true }).fill(price);

    const entity = await this.submitNestedForm(dialog, '/products/products', name, ['id', 'name']);
    await expect(this.form.getByText(name)).toBeVisible();
    return entity;
  }

  async selectFirstActivityAndExpectDuration(): Promise<void> {
    const activityInput = this.form.getByPlaceholder('Weitere Tätigkeiten...');
    await activityInput.fill('a');

    const summaryResponse = this.page.waitForResponse(
      (response) => response.url().includes('/customers/appointments/activitysummary') && response.ok(),
    );
    await activityInput.press('ArrowDown');
    await activityInput.press('Enter');
    const response = await summaryResponse;
    const authorization = (await response.request().allHeaders())['authorization'];
    const summaryRequest = await this.page.request.get(response.url(), {
      headers: authorization ? { Authorization: authorization } : undefined,
    });
    expect(summaryRequest.ok()).toBe(true);
    const summary = (await summaryRequest.json()) as { durationSum?: string };

    expect(summary.durationSum).toBeTruthy();
    await expect(this.form.getByText(summary.durationSum!, { exact: true })).toBeVisible();
  }

  async createAppointment(date: string, time: string, customerName: string): Promise<CreatedEntity> {
    await this.fillAndBlur('Datum', date);
    await this.fillAndBlur('Zeit', time);
    const saveResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'POST' && response.url().endsWith('/customers/appointments'),
    );
    await this.page.getByTitle('Jetzt speichern').click();
    const response = await saveResponse;
    expect(response.ok()).toBe(true);
    const authorization = await this.authorizationHeader(response);
    const id = await this.createdEntityId(
      response,
      '/customers/customers/appointments',
      customerName,
      ['id', 'customerName'],
      authorization,
    );
    return { authorization, id };
  }

  async deleteCurrentAppointment(): Promise<void> {
    await this.form.getByTitle('Jetzt löschen').click();
    const confirmation = this.page.getByRole('dialog').filter({ hasText: 'Kundentermin löschen' });
    const updateResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes('/customers/appointments/'),
    );
    await confirmation.getByRole('button', { name: 'Bestätigen', exact: true }).click();
    expect((await updateResponse).ok()).toBe(true);
  }

  private async fillAndBlur(label: string, value: string): Promise<void> {
    const input = this.form.getByLabel(label, { exact: true });
    await input.fill(value);
    await input.press('Tab');
  }

  private async authorizationHeader(response: import('@playwright/test').Response): Promise<string> {
    const authorization = (await response.request().allHeaders())['authorization'];
    if (!authorization) throw new Error('Missing Authorization header on customer appointment request.');
    return authorization;
  }
}
