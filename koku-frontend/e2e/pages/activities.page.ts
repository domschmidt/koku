import { expect, Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { MasterDataListPage } from './master-data-list.page';
import { waitForTwoAnimationFrames } from '../support/browser';

export class ActivitiesPage extends MasterDataListPage {
  constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  async open(): Promise<void> {
    await this.openMasterDataTab('Tätigkeiten', /\/manage\/activities(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.expectMasterDataList('Tätigkeiten');
  }

  async openNamedActivity(name: string): Promise<void> {
    await this.searchFor(name);
    const sourceResponse = this.page.waitForResponse(
      (response) =>
        response.request().method() === 'GET' &&
        /\/activities\/activities\/\d+$/.test(new URL(response.url()).pathname),
    );
    await this.resultItems.filter({ hasText: name }).click();
    expect((await sourceResponse).ok()).toBe(true);
    await expect(this.page.getByTestId('activity-form')).toBeVisible();
    await expect(this.page.getByTestId('activity-form').getByLabel('Name', { exact: true })).toBeEnabled();
  }

  async renameOpenedActivity(name: string): Promise<void> {
    const form = this.page.getByTestId('activity-form');
    const nameInput = form.getByLabel('Name', { exact: true });
    await nameInput.evaluate((element, nextName) => {
      const input = element as HTMLInputElement;
      input.value = nextName;
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }, name);
    await waitForTwoAnimationFrames(this.page);
    await expect(nameInput).toHaveValue(name);
    const responsePromise = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes('/activities/activities/'),
    );
    await this.page.getByTitle('Jetzt speichern').click();
    expect((await responsePromise).ok()).toBe(true);
    await expect(form.getByLabel('Name', { exact: true })).toHaveValue(name);
    await this.expectListItemText(name);
    await expect(this.page.getByTestId('list-inline-content').getByText(name, { exact: true }).first()).toBeVisible();
  }

  async deleteOpenedActivity(): Promise<void> {
    await this.executeDetailLifecycleAction('Jetzt löschen', 'Jetzt wiederherstellen');
  }

  async restoreOpenedActivity(): Promise<void> {
    await this.executeDetailLifecycleAction('Jetzt wiederherstellen', 'Jetzt löschen');
  }

  private async executeDetailLifecycleAction(actionTitle: string, expectedTitle: string): Promise<void> {
    const form = this.page.getByTestId('activity-form');
    const name = await form.getByLabel('Name', { exact: true }).inputValue();
    await form.getByTitle(actionTitle).click();
    const confirmation = this.page.getByRole('dialog').filter({ hasText: name });
    const responsePromise = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes('/activities/activities/'),
    );
    await confirmation.getByRole('button', { name: 'Bestätigen', exact: true }).click();
    expect((await responsePromise).ok()).toBe(true);
    await expect(form.getByTitle(expectedTitle)).toBeVisible();
  }
}
