import {LoginPage} from "../pages/login/login.page";
import {UserAdministrationPage} from "../pages/user/user-administration.page";
import {v1 as uuidV1} from "uuid";
import {UserManageDialog} from "../pages/user/user-manage.dialog";

describe('user-management', () => {

  it('allows to create and update users', () => {
    let loginPage = new LoginPage();
    loginPage.open();
    let welcomeComponentPage = loginPage.loginWithDefaultCredentials();
    let userAdministrationPage = welcomeComponentPage.getNavigation().navigateToPage('Administration', new UserAdministrationPage());
    let userManageDialog = userAdministrationPage.openCreateDialog();
    const randomUuid = uuidV1();
    const initialUsername = `CYPRESS-USER-${randomUuid}`;
    const initialPassword = `CYPRESS-PASSWORD-${randomUuid}`;
    const initialFirstname = `CYPRESS-FIRSTNAME-${randomUuid}`;
    const initialLastname = `CYPRESS-LASTNAME-${randomUuid}`;
    userManageDialog.writeUserDetails(
      initialUsername,
      initialFirstname,
      initialLastname,
      initialPassword
    )
    userAdministrationPage = userManageDialog.saveChanges(true);
    userManageDialog = userAdministrationPage.getPageSearch().searchAndOpen(initialFirstname, new UserManageDialog()) as UserManageDialog;
    userManageDialog.validateUsernameEquality(initialUsername);
    userManageDialog.validateFirstnameEquality(initialFirstname);
    userManageDialog.validateLastnameEquality(initialLastname);
    userManageDialog.close();
    userManageDialog = userAdministrationPage.getPageSearch().searchAndOpen(initialLastname, new UserManageDialog()) as UserManageDialog;
    userManageDialog.validateUsernameEquality(initialUsername);
    userManageDialog.validateFirstnameEquality(initialFirstname);
    userManageDialog.validateLastnameEquality(initialLastname);
    userManageDialog.close();
    userManageDialog = userAdministrationPage.getPageSearch().searchAndOpen(initialUsername, new UserManageDialog()) as UserManageDialog;
    userManageDialog.validateUsernameEquality(initialUsername);
    userManageDialog.validateFirstnameEquality(initialFirstname);
    userManageDialog.validateLastnameEquality(initialLastname);
    userManageDialog.close();

    userManageDialog = userAdministrationPage.openCreateDialog();
    const anotherRandomUuid = uuidV1();
    const anotherUsername = `CYPRESS-USER-${anotherRandomUuid}`;
    const anotherPassword = `CYPRESS-PASSWORD-${anotherRandomUuid}`;
    const anotherFirstname = `CYPRESS-FIRSTNAME-${anotherRandomUuid}`;
    const anotherLastname = `CYPRESS-LASTNAME-${anotherRandomUuid}`;
    const anotherUsernameChanged = `CYPRESS-USER-${anotherRandomUuid}-changed`;
    const anotherPasswordChanged = `CYPRESS-PASSWORD-${anotherRandomUuid}-changed`;
    const anotherFirstnameChanged = `CYPRESS-FIRSTNAME-${anotherRandomUuid}-changed`;
    const anotherLastnameChanged = `CYPRESS-LASTNAME-${anotherRandomUuid}-changed`;
    userManageDialog.writeUserDetails(
      anotherUsername,
      anotherFirstname,
      anotherLastname,
      anotherPassword
    )
    userAdministrationPage = userManageDialog.saveChanges(true);
    userManageDialog.close();
    userManageDialog = userAdministrationPage.getPageSearch().searchAndOpen(anotherUsername, new UserManageDialog()) as UserManageDialog;
    userManageDialog.writeUserDetails(anotherUsernameChanged, anotherFirstnameChanged, anotherLastnameChanged, anotherPasswordChanged);
    userManageDialog.saveChanges(false);
    userManageDialog.close()

    const userAdministrationPageSearch = userAdministrationPage.getPageSearch();
    userAdministrationPageSearch.search(initialUsername + 'unknown-user');
    userAdministrationPageSearch.validateResultCount(0);

    loginPage = userAdministrationPage.getNavigation().logout();
    welcomeComponentPage = loginPage.login(initialUsername, initialPassword);

    let myProfilePage = welcomeComponentPage.getNavigation().navigateToMyProfile();
    myProfilePage.validateUsernameEquality(initialUsername);
    myProfilePage.validateFirstnameEquality(initialFirstname);
    myProfilePage.validateLastnameEquality(initialLastname);
    const changedUserName = `${initialUsername}-changed`;
    const changedFirstname = `${initialFirstname}-changed`;
    const changedLastname = `${initialLastname}-changed`;
    const changedPassword = `${initialPassword}-changed`;
    myProfilePage.writeUserDetails(changedUserName, changedFirstname, changedLastname, changedPassword);
    myProfilePage.saveChanges();
    loginPage = myProfilePage.getNavigation().logout();
    welcomeComponentPage = loginPage.login(changedUserName, changedPassword);
    myProfilePage = welcomeComponentPage.getNavigation().navigateToMyProfile();
    myProfilePage.validateUsernameEquality(changedUserName);
    myProfilePage.validateFirstnameEquality(changedFirstname);
    myProfilePage.validateLastnameEquality(changedLastname);

    loginPage = myProfilePage.getNavigation().logout();
    welcomeComponentPage = loginPage.login(anotherUsernameChanged, anotherPasswordChanged);
    myProfilePage = welcomeComponentPage.getNavigation().navigateToMyProfile();
    myProfilePage.validateFirstnameEquality(anotherFirstnameChanged);
    myProfilePage.validateLastnameEquality(anotherLastnameChanged);
  });

});
