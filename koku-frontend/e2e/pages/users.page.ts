import { Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { ListWorkspacePage } from './list-workspace.page';

export class UsersPage extends ListWorkspacePage {
  constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  async open(): Promise<void> {
    await this.appShell.navigateTo('Administration', /\/administration\/users(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.expectShellTitle('Administration');
    await this.expectTabVisible('Nutzer');
  }
}
