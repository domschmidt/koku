import { expect, Locator, Page } from '@playwright/test';

export type MainNavigationEntry =
  | 'Administration'
  | 'Kalender'
  | 'Logout'
  | 'Mein Profil'
  | 'Stammdaten'
  | 'Statistik';

export class AppShellPage {
  private readonly drawerToggle: Locator;
  private readonly navigationRegion: Locator;
  private readonly navigationToggle: Locator;
  private readonly titleBar: Locator;

  constructor(private readonly page: Page) {
    this.drawerToggle = page.getByTestId('navigation-drawer-toggle');
    this.navigationRegion = page.getByRole('navigation', { name: 'Navigation' });
    this.navigationToggle = page.getByLabel('Navigation oeffnen');
    this.titleBar = page.getByTestId('page-title-bar');
  }

  async expectReady(timeout = 15_000): Promise<void> {
    await expect(this.navigationToggle).toBeVisible({ timeout });
  }

  async expectTitle(title: string): Promise<void> {
    await expect(this.titleBar.getByText(title, { exact: true })).toBeVisible();
  }

  async expectUrl(expectedUrl: RegExp): Promise<void> {
    await expect(this.page).toHaveURL(expectedUrl);
  }

  async isReady(timeout = 3_000): Promise<boolean> {
    return this.navigationToggle.isVisible({ timeout }).catch(() => false);
  }

  async openNavigation(): Promise<void> {
    if (!(await this.drawerToggle.isChecked())) {
      await this.navigationToggle.click();
    }

    await expect(this.navigationRegion).toBeVisible();
  }

  async expectNavigationEntryVisible(entryName: MainNavigationEntry): Promise<void> {
    await this.openNavigation();
    await expect(this.navigationEntry(entryName)).toBeVisible();
  }

  async navigateTo(entryName: MainNavigationEntry, expectedUrl: RegExp): Promise<void> {
    await this.openNavigation();
    await this.navigationEntry(entryName).click();
    await this.expectUrl(expectedUrl);
  }

  private navigationEntry(entryName: MainNavigationEntry): Locator {
    return this.navigationRegion.getByRole('link', { name: exactText(entryName) });
  }
}

function exactText(value: string): RegExp {
  return new RegExp(`^${escapeRegExp(value)}$`);
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, String.raw`\$&`);
}
