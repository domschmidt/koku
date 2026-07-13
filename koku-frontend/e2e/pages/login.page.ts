import { expect, Locator, Page } from '@playwright/test';
import { readE2eCredentials } from '../support/credentials';
import { AppShellPage } from './app-shell.page';

export class LoginPage {
  private readonly passwordInput: Locator;
  private readonly submitButton: Locator;
  private readonly usernameInput: Locator;

  constructor(
    private readonly page: Page,
    private readonly appShell: AppShellPage,
  ) {
    this.passwordInput = page.locator('input[name="password"], input#password').first();
    this.submitButton = page.locator('#kc-login, input[type="submit"], button[type="submit"]').first();
    this.usernameInput = page.locator('input[name="username"], input#username').first();
  }

  async signIn(path = '/'): Promise<void> {
    const credentials = readE2eCredentials();

    await this.page.goto(path);
    await this.page.waitForLoadState('domcontentloaded');

    if (await this.appShell.isReady()) {
      return;
    }

    await expect(this.usernameInput).toBeVisible({ timeout: 30_000 });
    await this.usernameInput.fill(credentials.username);
    await this.passwordInput.fill(credentials.password);
    await this.submitButton.click();

    await this.appShell.expectReady(60_000);
  }
}
