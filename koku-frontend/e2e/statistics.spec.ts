import { test } from './fixtures/koku-test';

test.describe('statistics', () => {
  test.beforeEach(async ({ loginPage, statisticsPage }) => {
    await loginPage.signIn();
    await statisticsPage.open();
  });

  test('renders all dashboard KPI panels and the revenue chart', async ({ statisticsPage }) => {
    await statisticsPage.expectDashboardPanels();
  });

  for (const chart of [
    { endpoint: '/customers/appointments/statistics', route: 'revenues', tab: 'Umsätze' },
    { endpoint: '/customers/products/statistics', route: 'products', tab: 'Produkte' },
    { endpoint: '/customers/activities/statistics', route: 'activities', tab: 'Tätigkeiten' },
    { endpoint: '/customers/customers/statistics', route: 'customers', tab: 'Kunden' },
  ]) {
    test(`${chart.tab} applies its month range`, async ({ statisticsPage }) => {
      await statisticsPage.openChart(chart.tab, chart.route);
      await statisticsPage.changeDateRange('2026-01', '2026-06', chart.endpoint);
      await statisticsPage.expectRenderedChart();
    });
  }
});
