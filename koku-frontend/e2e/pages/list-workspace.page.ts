import { expect, Locator, Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';

export abstract class ListWorkspacePage {
  private readonly emptyState: Locator;
  protected readonly resultItems: Locator;
  protected readonly searchInput: Locator;

  protected constructor(
    protected readonly page: Page,
    protected readonly appShell: AppShellPage,
  ) {
    this.emptyState = page.getByTestId('list-empty-state');
    this.resultItems = page.getByTestId('list-item-trigger');
    this.searchInput = page.getByRole('textbox', { name: 'Suche', exact: true });
  }

  async expectResultsVisible(): Promise<void> {
    await expect(this.resultItems).not.toHaveCount(0);
  }

  async searchFor(term: string): Promise<void> {
    const searchRequest = this.page.waitForResponse((response) => {
      if (response.request().method() !== 'POST' || !response.url().includes('/query')) return false;
      const requestBody = response.request().postDataJSON() as { globalSearchTerm?: string } | null;
      return requestBody?.globalSearchTerm === term;
    });

    await this.searchInput.evaluate((element, searchTerm) => {
      const input = element as HTMLInputElement;
      input.value = searchTerm;
      input.dispatchEvent(new Event('input', { bubbles: true }));
    }, term);
    const response = await searchRequest;
    expect(response.ok()).toBe(true);
  }

  async expectNoResults(): Promise<void> {
    await expect(this.emptyState).toBeVisible();
    await expect(this.resultItems).toHaveCount(0);
  }

  async clearSearch(): Promise<void> {
    await this.searchFor('');
    await expect(this.searchInput).toHaveValue('');
  }

  async openAdvancedFilters(): Promise<void> {
    const toggle = this.page.getByRole('button', { name: 'Erweiterte Filter' });
    await toggle.click();
    await expect(toggle).toHaveAttribute('aria-expanded', 'true');
    await expect(this.page.getByTestId('list-advanced-filters')).toBeVisible();
  }

  async cycleToggleFilter(label: string): Promise<void> {
    const queryResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'POST' && response.url().includes('/query'),
    );
    await this.page.getByRole('checkbox', { name: label, exact: true }).click();
    expect((await queryResponse).ok()).toBe(true);
  }

  async openFirstResult(): Promise<string> {
    await this.expectResultsVisible();
    const firstResult = this.resultItems.first();
    const resultText = (await firstResult.innerText()).trim();

    await firstResult.click();
    await expect(this.page.getByTestId('list-inline-content').first()).toBeVisible();

    return resultText;
  }

  async deleteFromList(itemText: string): Promise<void> {
    await this.executeListItemAction(itemText, 'Löschen', 'DELETE');
  }

  async restoreFromList(itemText: string): Promise<void> {
    await this.executeListItemAction(itemText, 'Wiederherstellen', 'PUT');
  }

  async expectListItemText(text: string): Promise<void> {
    await expect(this.resultItems.filter({ hasText: text })).toBeVisible();
  }

  protected async expectSearchVisible(): Promise<void> {
    await expect(this.searchInput).toBeVisible();
  }

  protected async expectShellTitle(title: string): Promise<void> {
    await this.appShell.expectTitle(title);
  }

  protected async expectTabVisible(tabName: string | RegExp): Promise<void> {
    await expect(this.page.getByRole('tab', { name: tabName })).toBeVisible();
  }

  protected async openTab(tabName: string, expectedUrl: RegExp): Promise<void> {
    await this.page.getByRole('tab', { name: tabName }).click();
    await this.appShell.expectUrl(expectedUrl);
  }

  private async executeListItemAction(itemText: string, actionName: string, method: string): Promise<void> {
    const item = this.resultItems.filter({ hasText: itemText });
    const responsePromise = this.page.waitForResponse((response) => response.request().method() === method);
    const labelledAction = item.getByRole('button', { name: actionName, exact: true });
    const action = (await labelledAction.count()) ? labelledAction : item.getByRole('button').last();
    await action.click();
    const confirmation = this.page.getByRole('dialog').filter({ hasText: itemText });
    await confirmation.getByRole('button', { name: 'Bestätigen', exact: true }).click();
    expect((await responsePromise).ok()).toBe(true);
    await expect(confirmation).toBeHidden();
  }
}
