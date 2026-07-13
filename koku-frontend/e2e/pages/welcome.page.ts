import { AppShellPage } from './app-shell.page';

export class WelcomePage {
  constructor(private readonly appShell: AppShellPage) {}

  async expectLoaded(): Promise<void> {
    await this.appShell.expectUrl(/\/welcome(?:[?#]|$)/);
    await this.appShell.expectTitle('Willkommen');
    await this.appShell.expectReady();
  }
}
