import {LoginPage} from "../pages/login/login.page";
import {UserAdministrationPage} from "../pages/user/user-administration.page";
import {v1 as uuidV1} from "uuid";
import {KokuTabPage} from "../page-helper/types/koku-tab.page";
import {CustomerAdministrationPage} from "../pages/customer/customer-administration.page";


describe('customer-management', () => {

  it('allows to create and update customers', () => {
    let loginPage = new LoginPage();
    loginPage.open();
    let welcomeComponentPage = loginPage.loginWithDefaultCredentials();
    let customerPage = welcomeComponentPage.getNavigation().navigateToPage('Stammdaten', new CustomerAdministrationPage());
    let userManageDialog = customerPage.openCreateDialog();
  })

})
