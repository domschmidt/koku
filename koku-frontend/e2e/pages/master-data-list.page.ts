import { Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { ListWorkspacePage } from './list-workspace.page';

export abstract class MasterDataListPage extends ListWorkspacePage {
  protected constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  protected async openMasterDataTab(tabName: string, expectedUrl: RegExp): Promise<void> {
    await this.appShell.navigateTo('Stammdaten', /\/manage\/customers(?:[/?#]|$)/);
    await this.openTab(tabName, expectedUrl);
  }

  protected async expectMasterDataList(tabName: string | RegExp): Promise<void> {
    await this.expectShellTitle('Stammdaten');
    await this.expectTabVisible(tabName);
    await this.expectSearchVisible();
  }
}
