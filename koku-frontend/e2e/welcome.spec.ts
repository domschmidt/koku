import { test } from './fixtures/koku-test';

test.describe('welcome', () => {
  test('opens after a real login', async ({ loginPage, welcomePage }) => {
    await loginPage.signIn();
    await welcomePage.expectLoaded();
  });
});
