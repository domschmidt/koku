import { Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { ListWorkspacePage } from './list-workspace.page';

export class ProductsPage extends ListWorkspacePage {
  constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  async open(): Promise<void> {
    await this.appShell.navigateTo('Stammdaten', /\/manage\/customers(?:[/?#]|$)/);
    await this.openTab('Produkte', /\/manage\/products(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.expectShellTitle('Stammdaten');
    await this.expectTabVisible('Produkte');
    await this.expectSearchVisible();
  }
}
