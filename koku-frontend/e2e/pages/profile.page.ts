import { expect, Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';

export class ProfilePage {
  constructor(
    private readonly page: Page,
    private readonly appShell: AppShellPage,
  ) {}

  async open(): Promise<void> {
    await this.appShell.navigateTo('Mein Profil', /\/myprofile(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.appShell.expectTitle('Mein Profil');
    await expect(this.page.getByTestId('user-form')).toBeVisible();
  }

  async expectLogoutReachable(): Promise<void> {
    await this.appShell.expectNavigationEntryVisible('Logout');
  }
}
