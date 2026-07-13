import { expect, Page, Response } from '@playwright/test';
import { CreatedEntity, FormPage } from './form.page';

export class PrivateAppointmentPage extends FormPage {
  constructor(page: Page) {
    super(page, page.getByTestId('user-appointment-form'));
  }

  async create(description: string): Promise<CreatedEntity> {
    await this.form.getByLabel('Beschreibung', { exact: true }).fill(description);
    await this.fillAndBlur('Datum von', '20.07.2026');
    await this.fillAndBlur('Zeit von', '10:15');
    await this.fillAndBlur('Datum bis', '20.07.2026');
    await this.fillAndBlur('Zeit bis', '11:45');

    const saveResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'POST' && response.url().endsWith('/users/appointments'),
    );
    await this.page.getByTitle('Jetzt speichern').click();
    const response = await saveResponse;
    expect(response.ok()).toBe(true);
    const authorization = await authorizationHeader(response);
    const payload = await this.lookupAppointment(description, authorization);
    return { authorization, id: payload.id };
  }

  private async fillAndBlur(label: string, value: string): Promise<void> {
    const input = this.form.getByLabel(label, { exact: true });
    await input.fill(value);
    await input.press('Tab');
  }

  private async lookupAppointment(description: string, authorization: string) {
    let appointment = { description: '', endTime: '', id: 0, startTime: '' };
    await expect
      .poll(async () => {
        const result = await this.page.request.post('/services/users/users/appointments/query', {
          headers: { Authorization: authorization },
          data: {
            fieldPredicates: {},
            fieldSelection: ['id', 'description', 'startTime', 'endTime'],
            globalSearchTerm: description,
            limit: 10,
            page: 0,
          },
        });
        if (!result.ok()) return 0;
        const body = (await result.json()) as { results?: Array<{ id?: string; values?: typeof appointment }> };
        const item = body.results?.[0];
        appointment = { ...appointment, ...item?.values, id: Number(item?.id ?? item?.values?.id) };
        return appointment.id;
      })
      .toBeGreaterThan(0);
    expect(appointment.description).toBe(description);
    expect(appointment.startTime).toContain('10:15');
    expect(appointment.endTime).toContain('11:45');
    return appointment;
  }
}

async function authorizationHeader(response: Response): Promise<string> {
  const authorization = (await response.request().allHeaders())['authorization'];
  if (!authorization) {
    throw new Error('Missing Authorization header on private appointment request.');
  }
  return authorization;
}
