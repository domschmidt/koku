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

  async moveAppointment(appointmentId: number, targetIsoDate: string): Promise<void> {
    const source = this.appointment(appointmentId);
    const target = this.page.locator(`[role="gridcell"][data-date="${targetIsoDate}"]`);
    await source.scrollIntoViewIfNeeded();

    const updateResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'PUT' && response.url().includes(`/appointments/${appointmentId}`),
      { timeout: 20_000 },
    );

    // FullCalendar tracks the pointer globally, so drive the mouse directly: the
    // non-business overlay of neighbouring day cells intercepts pointer events around the
    // drop target, which makes locator.dragTo fail its actionability check.
    const sourceBox = await source.boundingBox();
    const targetBox = await target.boundingBox();
    if (!sourceBox || !targetBox) {
      throw new Error(`Cannot drag appointment ${appointmentId} onto ${targetIsoDate}`);
    }
    await this.page.mouse.move(sourceBox.x + sourceBox.width / 2, sourceBox.y + sourceBox.height / 2);
    await this.page.mouse.down();
    await this.page.mouse.move(targetBox.x + targetBox.width / 2, targetBox.y + targetBox.height / 2, { steps: 15 });
    await this.page.mouse.up();

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
