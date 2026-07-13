import { defineConfig, devices } from '@playwright/test';

const baseURL = new URL(process.env['KOKU_E2E_BASE_URL'] ?? 'https://127.0.0.1:4200').toString();

export default defineConfig({
  testDir: './e2e',
  fullyParallel: !process.env['CI'],
  globalSetup: './e2e/support/global-setup.ts',
  timeout: 60_000,
  expect: {
    timeout: 15_000,
  },
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 1 : 4,
  reporter: process.env['CI']
    ? [['line'], ['html', { open: 'never' }], ['junit', { outputFile: 'test-results/e2e-junit.xml' }]]
    : [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL,
    ignoreHTTPSErrors: true,
    actionTimeout: 15_000,
    navigationTimeout: 45_000,
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
