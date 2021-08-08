import {KokuNavigatablePage} from "../types/koku-navgatable-internal.page";

export class KokuTabBar {

  private readonly SELECTOR_TAB_PAGE = '.koku-tab-page'
  private readonly SELECTOR_TAB_ITEM = '.koku-tab-page__item'

  openTab<T extends KokuNavigatablePage>(name: string,
                                         targetPageClazz: T): T {
    cy.get(this.SELECTOR_TAB_ITEM).contains(name).click();
    targetPageClazz.validatePageOpened();
    return targetPageClazz;
  }

}
