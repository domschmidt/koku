import { expect, Locator, Page, Response } from '@playwright/test';

export interface CreatedEntity {
  readonly authorization: string;
  readonly id: number;
}

export abstract class FormPage {
  protected constructor(
    protected readonly page: Page,
    protected readonly form: Locator,
  ) {}

  async expectLoaded(): Promise<void> {
    await expect(this.form).toBeVisible();
  }

  protected async openNestedForm(actionTitle: string, formUrlSuffix: string, formTestId: string): Promise<Locator> {
    const formResponse = this.page.waitForResponse(
      (response) => response.url().endsWith(formUrlSuffix) && response.ok(),
    );

    await this.form.getByTitle(actionTitle).click();
    await formResponse;
    const nestedForm = this.page.getByTestId(formTestId);
    await expect(nestedForm).toBeVisible();
    return this.page.getByRole('dialog').filter({ has: nestedForm });
  }

  protected async submitNestedForm(
    dialog: Locator,
    endpointMarker: string,
    lookupTerm: string,
    lookupFields: string[],
  ): Promise<CreatedEntity> {
    const saveResponse = this.page.waitForResponse(
      (response) => response.request().method() === 'POST' && response.url().includes(endpointMarker),
    );

    await dialog.getByTitle('Jetzt speichern').click();
    const response = await saveResponse;
    expect(response.ok()).toBe(true);
    const authorization = await authorizationHeader(response);
    const id = await this.createdEntityId(response, endpointMarker, lookupTerm, lookupFields, authorization);
    await expect(dialog).toBeHidden();

    return {
      authorization,
      id,
    };
  }

  protected async createdEntityId(
    response: Response,
    endpointMarker: string,
    lookupTerm: string,
    lookupFields: string[],
    authorization: string,
  ): Promise<number> {
    try {
      const payload = (await response.json()) as { id: number };
      if (payload.id) return payload.id;
    } catch {
      // Chromium can release a proxied response body before Playwright reads it.
    }

    let recoveredId = 0;
    await expect
      .poll(
        async () => {
          const queryResponse = await this.page.request.post(`/services${endpointMarker}/query`, {
            headers: { Authorization: authorization },
            data: { fieldPredicates: {}, fieldSelection: lookupFields, globalSearchTerm: lookupTerm, limit: 10, page: 0 },
          });
          if (!queryResponse.ok()) return 0;
          const queryResult = (await queryResponse.json()) as {
            results?: Array<{ id?: string; values?: { id?: number } }>;
          };
          recoveredId = Number(queryResult.results?.[0]?.id ?? queryResult.results?.[0]?.values?.id);
          return recoveredId;
        },
        { timeout: 15_000 },
      )
      .toBeGreaterThan(0);
    return recoveredId;
  }
}

async function authorizationHeader(response: Response): Promise<string> {
  const headers = await response.request().allHeaders();
  const authorization = headers['authorization'];
  if (!authorization) {
    throw new Error('The authenticated form request did not contain an Authorization header.');
  }
  return authorization;
}
