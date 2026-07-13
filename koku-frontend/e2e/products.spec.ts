import { test } from './fixtures/koku-test';

test.describe('products', () => {
  test.beforeEach(async ({ loginPage, productsPage }) => {
    await loginPage.signIn();
    await productsPage.open();
  });

  test('shows the product list', async ({ productsPage }) => {
    await productsPage.expectLoaded();
    await productsPage.expectResultsVisible();
  });

  test('filters products and restores the complete list', async ({ productsPage }) => {
    await productsPage.expectResultsVisible();

    await productsPage.searchFor('__koku_e2e_product_without_match__');
    await productsPage.expectNoResults();

    await productsPage.clearSearch();
    await productsPage.expectResultsVisible();
  });

  test('opens an existing product detail', async ({ productsPage }) => {
    await productsPage.openFirstResult();
  });
});
