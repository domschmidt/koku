import {LoginPage} from "../pages/login/login.page";
import {v1 as uuidV1} from "uuid";
import {CustomerAdministrationPage} from "../pages/customer/customer-administration.page";
import {ProductAdministrationPage} from "../pages/product/product-administration.page";
import {padStart} from 'lodash'
import {ProductManageDialog} from "../pages/product/product-manage.dialog";


describe('product-management', () => {

  it('allows to create and update products', () => {
    let loginPage = new LoginPage();
    loginPage.open();
    let welcomeComponentPage = loginPage.loginWithDefaultCredentials();
    let customerPage = welcomeComponentPage.getNavigation().navigateToPage('Stammdaten', new CustomerAdministrationPage());
    let productAdministrationPage = customerPage.getTabBar().openTab('Produkte', new ProductAdministrationPage());
    let productManageDialog = productAdministrationPage.openCreateDialog();
    const randomUuid = uuidV1();
    let initialDescription = `CYPRESS-PRODUCT-${randomUuid}`;
    let initialPrice = `${Math.floor(Math.random() * (999 - 1)) + 1}.${padStart(String(Math.floor(Math.random() * (99 - 1)) + 1), 2, '0')}`;
    productManageDialog.writeDescription(initialDescription);
    productManageDialog.writePrice(initialPrice);
    const productManufacturerSelectDialog = productManageDialog.openManufacturerSelection();
    const productManufacturerManageDialog = productManufacturerSelectDialog.openCreateDialog();
    let productManufacturerName = `CYPRESS-PRODUCTMANUFACTURER-${randomUuid}`;
    productManufacturerManageDialog.writeName(productManufacturerName);
    productManufacturerManageDialog.saveChanges(true);
    productManufacturerSelectDialog.waitUntilClosed();
    productManageDialog.saveChanges(true);
    productManageDialog = productAdministrationPage.getPageSearch().searchAndOpen(initialDescription, new ProductManageDialog()) as ProductManageDialog;
    productManageDialog.validateDescription(initialDescription);
    productManageDialog.validatePrice(initialPrice);

    let descriptionChanged = `${initialDescription}-changed`;
    let priceChanged = `${Math.floor(Math.random() * (999 - 1)) + 1}.${padStart(String(Math.floor(Math.random() * (99 - 1)) + 1), 2, '0')}`;
    productManageDialog.writeDescription(descriptionChanged);
    productManageDialog.writePrice(priceChanged);
    productManageDialog.saveChanges(false);

    productManageDialog = productAdministrationPage.getPageSearch().searchAndOpen(descriptionChanged, new ProductManageDialog()) as ProductManageDialog;
    productManageDialog.validateDescription(descriptionChanged);
    productManageDialog.validatePrice(priceChanged);
    productManageDialog.close();
  })

})
