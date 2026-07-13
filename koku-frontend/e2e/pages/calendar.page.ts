import { expect, Page } from '@playwright/test';
import { AppShellPage } from './app-shell.page';

type CalendarView = 'Tag' | 'Woche' | 'Monat';
type CalendarDirection = 'Zurück' | 'Weiter';

export class CalendarPage {
  constructor(
    private readonly page: Page,
    private readonly appShell: AppShellPage,
  ) {}

  async open(): Promise<void> {
    await this.appShell.navigateTo('Kalender', /\/calendar(?:[/?#]|$)/);
  }

  async expectLoaded(): Promise<void> {
    await this.appShell.expectTitle('Kalender');
    await expect(this.page.getByTestId('calendar')).toBeVisible();
  }

  async openNewAppointment(): Promise<void> {
    await this.page.getByTitle(/Neues Ereignis/).click();
    await this.appShell.expectUrl(/\/calendar\/new\/customer-appointment(?:[/?#]|$)/);
  }

  async openNewPrivateAppointment(): Promise<void> {
    await this.openNewAppointment();
    await this.page.getByRole('button', { name: 'Privater Termin', exact: true }).click();
    await this.appShell.expectUrl(/\/calendar\/new\/user-appointment(?:[/?#]|$)/);
  }

  async expectAppointmentVisible(appointmentId: number): Promise<void> {
    await expect(this.appointment(appointmentId)).toBeVisible();
  }

  async moveAppointment(appointmentId: number, targetDateName: string): Promise<void> {
    const updateResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes(`/appointments/${appointmentId}`),
    );
    await this.appointment(appointmentId).dragTo(this.page.getByRole('gridcell', { name: targetDateName }));
    expect((await updateResponse).ok()).toBe(true);
  }

  async openAppointment(appointmentId: number): Promise<void> {
    await this.appointment(appointmentId).click();
  }

  async selectView(view: CalendarView): Promise<void> {
    const button = this.page.getByRole('button', { name: view, exact: true });
    await button.click();
    await expect(button).toHaveAttribute('aria-pressed', 'true');
    await expect(this.periodInput(view)).toBeVisible();
  }

  async expectPeriodChanges(view: CalendarView, direction: CalendarDirection): Promise<void> {
    const input = this.periodInput(view);
    const previousValue = await input.inputValue();
    await this.page.getByRole('button', { name: direction, exact: true }).click();
    await expect(input).not.toHaveValue(previousValue);
  }

  private periodInput(view: CalendarView) {
    return this.page.getByRole('textbox', { name: view === 'Tag' ? 'Datum' : view, exact: true });
  }

  private appointment(appointmentId: number) {
    return this.page.getByTestId(`calendar-event-customer-appointments-${appointmentId}`);
  }
}
