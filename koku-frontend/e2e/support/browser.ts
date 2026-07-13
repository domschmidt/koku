import { Page } from '@playwright/test';

export async function waitForTwoAnimationFrames(page: Page): Promise<void> {
  await waitForAnimationFrame(page);
  await waitForAnimationFrame(page);
}

const waitForAnimationFrame = (page: Page): Promise<void> =>
  page.evaluate(() => new Promise<void>((resolve) => requestAnimationFrame(() => resolve())));
