import { test as base } from '@playwright/test';
import { AppShellPage } from '../pages/app-shell.page';
import { ActivitiesPage } from '../pages/activities.page';
import { ActivityStepsPage } from '../pages/activity-steps.page';
import { CalendarPage } from '../pages/calendar.page';
import { CustomersPage } from '../pages/customers.page';
import { CustomerAppointmentPage } from '../pages/customer-appointment.page';
import { LoginPage } from '../pages/login.page';
import { ProductsPage } from '../pages/products.page';
import { ProductManufacturersPage } from '../pages/product-manufacturers.page';
import { PromotionsPage } from '../pages/promotions.page';
import { PrivateAppointmentPage } from '../pages/private-appointment.page';
import { ProfilePage } from '../pages/profile.page';
import { StatisticsPage } from '../pages/statistics.page';
import { UsersPage } from '../pages/users.page';
import { WelcomePage } from '../pages/welcome.page';
import { hasE2eCredentials } from '../support/credentials';

interface KokuPages {
  readonly authenticatedEnvironment: void;
  readonly activitiesPage: ActivitiesPage;
  readonly activityStepsPage: ActivityStepsPage;
  readonly appShell: AppShellPage;
  readonly calendarPage: CalendarPage;
  readonly customersPage: CustomersPage;
  readonly customerAppointmentPage: CustomerAppointmentPage;
  readonly loginPage: LoginPage;
  readonly productsPage: ProductsPage;
  readonly productManufacturersPage: ProductManufacturersPage;
  readonly promotionsPage: PromotionsPage;
  readonly privateAppointmentPage: PrivateAppointmentPage;
  readonly profilePage: ProfilePage;
  readonly statisticsPage: StatisticsPage;
  readonly usersPage: UsersPage;
  readonly welcomePage: WelcomePage;
}

const test = base.extend<KokuPages>({
  authenticatedEnvironment: [
    async ({}, use, testInfo) => {
      testInfo.skip(
        !hasE2eCredentials(),
        'Set KOKU_E2E_USERNAME and KOKU_E2E_PASSWORD to run authenticated E2E tests.',
      );
      await use();
    },
    { auto: true },
  ],
  activitiesPage: async ({ page, appShell }, use) => {
    await use(new ActivitiesPage(page, appShell));
  },
  activityStepsPage: async ({ page, appShell }, use) => {
    await use(new ActivityStepsPage(page, appShell));
  },
  appShell: async ({ page }, use) => {
    await use(new AppShellPage(page));
  },
  loginPage: async ({ page, appShell }, use) => {
    await use(new LoginPage(page, appShell));
  },
  welcomePage: async ({ appShell }, use) => {
    await use(new WelcomePage(appShell));
  },
  calendarPage: async ({ page, appShell }, use) => {
    await use(new CalendarPage(page, appShell));
  },
  customersPage: async ({ page, appShell }, use) => {
    await use(new CustomersPage(page, appShell));
  },
  customerAppointmentPage: async ({ page }, use) => {
    await use(new CustomerAppointmentPage(page));
  },
  productsPage: async ({ page, appShell }, use) => {
    await use(new ProductsPage(page, appShell));
  },
  productManufacturersPage: async ({ page, appShell }, use) => {
    await use(new ProductManufacturersPage(page, appShell));
  },
  promotionsPage: async ({ page, appShell }, use) => {
    await use(new PromotionsPage(page, appShell));
  },
  privateAppointmentPage: async ({ page }, use) => {
    await use(new PrivateAppointmentPage(page));
  },
  usersPage: async ({ page, appShell }, use) => {
    await use(new UsersPage(page, appShell));
  },
  statisticsPage: async ({ page, appShell }, use) => {
    await use(new StatisticsPage(page, appShell));
  },
  profilePage: async ({ page, appShell }, use) => {
    await use(new ProfilePage(page, appShell));
  },
});

export { test };
export { expect } from '@playwright/test';
