import { Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';
import { MasterDataListPage } from './master-data-list.page';

export class PromotionsPage extends MasterDataListPage {
  constructor(page: Page, appShell: AppShellPage) {
    super(page, appShell);
  }

  async open(): Promise<void> {
    await this.openMasterDataTab('Aktionen', /\/manage\/promotions(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.expectMasterDataList('Aktionen');
  }
}
