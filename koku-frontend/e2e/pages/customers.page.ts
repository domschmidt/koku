import { expect, Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { ListWorkspacePage } from './list-workspace.page';
import { waitForTwoAnimationFrames } from '../support/browser';

export class CustomersPage extends ListWorkspacePage {
  constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  async open(): Promise<void> {
    await this.appShell.navigateTo('Stammdaten', /\/manage\/customers(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.expectShellTitle('Stammdaten');
    await this.expectTabVisible('Kunden');
    await this.expectSearchVisible();
  }

  async deleteOpenedCustomer(): Promise<void> {
    await this.submitCustomerLifecycleAction('Jetzt löschen', 'Kunde löschen');
    await expect(this.page.getByTestId('customer-form').getByTitle('Jetzt wiederherstellen')).toBeVisible();
  }

  async restoreOpenedCustomer(): Promise<void> {
    await this.submitCustomerLifecycleAction('Jetzt wiederherstellen', 'Kunde wiederherstellen');
    await expect(this.page.getByTestId('customer-form').getByTitle('Jetzt löschen')).toBeVisible();
  }

  async renameOpenedCustomer(firstName: string, lastName: string): Promise<void> {
    const form = this.page.getByTestId('customer-form');
    await this.changeControlledInput(form.getByLabel('Vorname', { exact: true }), firstName);
    await this.changeControlledInput(form.getByLabel('Nachname', { exact: true }), lastName);
    const responsePromise = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes('/customers/customers/'),
    );
    await this.page.getByTitle('Jetzt speichern').click();
    expect((await responsePromise).ok()).toBe(true);
    const fullName = `${firstName} ${lastName}`;
    await expect(form.getByLabel('Vorname', { exact: true })).toHaveValue(firstName);
    await expect(form.getByLabel('Nachname', { exact: true })).toHaveValue(lastName);
    await this.expectListItemText(fullName);
    await expect(this.page.getByTestId('list-inline-content').getByText(fullName, { exact: true }).first()).toBeVisible();
  }

  async openNamedCustomer(name: string): Promise<void> {
    await this.searchFor(name);
    const sourceResponse = this.page.waitForResponse(
      (response) =>
        response.request().method() === 'GET' && /\/customers\/customers\/\d+$/.test(new URL(response.url()).pathname),
    );
    await this.resultItems.filter({ hasText: name }).click();
    expect((await sourceResponse).ok()).toBe(true);
    await expect(this.page.getByTestId('customer-form').getByLabel('Vorname', { exact: true })).toBeEnabled();
  }

  async expectCustomerVisible(name: string): Promise<void> {
    await this.searchFor(name);
    await expect(this.page.getByTestId('list-item-trigger').filter({ hasText: name })).toBeVisible();
  }

  async expectCustomerHidden(name: string): Promise<void> {
    await this.searchFor(name);
    await this.expectNoResults();
  }

  private async submitCustomerLifecycleAction(buttonTitle: string, dialogHeadline: string): Promise<void> {
    const form = this.page.getByTestId('customer-form');
    await form.getByTitle(buttonTitle).click();
    const confirmation = this.page.getByRole('dialog').filter({ hasText: dialogHeadline });
    const responsePromise = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes('/customers/customers/'),
    );
    await confirmation.getByRole('button', { name: 'Bestätigen', exact: true }).click();
    expect((await responsePromise).ok()).toBe(true);
    await expect(confirmation).toBeHidden();
  }

  private async changeControlledInput(input: import('@playwright/test').Locator, value: string): Promise<void> {
    await input.evaluate((element, nextValue) => {
      const nativeInput = element as HTMLInputElement;
      nativeInput.value = nextValue;
      nativeInput.dispatchEvent(new Event('change', { bubbles: true }));
    }, value);
    await waitForTwoAnimationFrames(this.page);
    await expect(input).toHaveValue(value);
  }
}
