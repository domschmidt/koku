import { expect, Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { ListWorkspacePage } from './list-workspace.page';

export class StatisticsPage extends ListWorkspacePage {
  constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  async open(): Promise<void> {
    await this.appShell.navigateTo('Statistik', /\/statistics(?:\/dashboard)?(?:[/?#]|$)/);
  }

  async expectDashboard(): Promise<void> {
    await this.expectShellTitle('Statistik');
    await this.expectTabVisible(/^(Uebersicht|Übersicht|Ãœbersicht)$/);
  }

  async expectDashboardPanels(): Promise<void> {
    await expect(this.page.getByTestId('dashboard')).toBeVisible();
    await expect(this.page.getByTestId('dashboard-text-panel')).toHaveCount(7);
    await expect(this.page.getByTestId('chart')).toBeVisible();
  }

  async openChart(tabName: string, route: string): Promise<void> {
    await this.openTab(tabName, new RegExp(`/statistics/${route}(?:[/?#]|$)`));
    await expect(this.page.getByTestId('chart-filters')).toBeVisible();
    await expect(this.page.getByTestId('chart')).toBeVisible();
  }

  async changeDateRange(from: string, to: string, endpoint: string): Promise<void> {
    await this.changeMonthFilter('Von', from, endpoint, { start: from });
    await this.changeMonthFilter('Bis', to, endpoint, { end: to, start: from });
  }

  async expectRenderedChart(): Promise<void> {
    await expect(this.page.getByRole('application', { name: /^[^.]*[.] bar chart$/ })).toBeVisible();
  }

  private async changeMonthFilter(
    label: string,
    value: string,
    endpoint: string,
    expectedParameters: Record<string, string>,
  ): Promise<void> {
    const responsePromise = this.page.waitForResponse((response) => {
      const url = new URL(response.url());
      return (
        response.request().method() === 'GET' &&
        url.pathname.endsWith(endpoint) &&
        Object.entries(expectedParameters).every(([name, expected]) => url.searchParams.get(name) === expected)
      );
    });
    const labelledPicker = this.page
      .getByLabel(`${label} Monat auswaehlen`, { exact: true })
      .and(this.page.locator('input'));
    let picker = labelledPicker;
    if (!(await labelledPicker.count())) {
      const pickerIndex = label === 'Von' ? 0 : 1;
      picker = this.page
        .getByTestId('chart-filters')
        .getByLabel('Monat auswaehlen', { exact: true })
        .and(this.page.locator('input'))
        .nth(pickerIndex);
    }
    await picker.evaluate((element, pickerValue) => {
      const input = element as HTMLInputElement;
      input.value = pickerValue;
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }, value);
    expect((await responsePromise).ok()).toBe(true);
  }
}
