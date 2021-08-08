import {KokuDialog} from "../dialogs/koku.dialog";

export class KokuPageSearch {

  private readonly expectedSearchCallUrl: string;
  private readonly expectedItemLoadCallUrl: string;
  private readonly searchFieldSelector: string;
  private readonly resultsSelector: string;

  constructor(
    expectedSearchCallUrl: string,
    expectedItemLoadCallUrl: string,
    searchFieldSelector: string,
    resultsSelector: string
  ) {
    this.expectedSearchCallUrl = expectedSearchCallUrl;
    this.expectedItemLoadCallUrl = expectedItemLoadCallUrl;
    this.searchFieldSelector = searchFieldSelector;
    this.resultsSelector = resultsSelector;
  }

  search(searchTerm: string) {
    return cy.intercept({
        url: this.expectedSearchCallUrl + `?search=${searchTerm}`,
        method: 'GET'
      }
    ).as('searchCall')
      .get(this.searchFieldSelector).clear().type(searchTerm).wait('@searchCall');
  }

  searchAndOpen(searchTerm: string,
                targetDialogClazz: KokuDialog) {
    this.search(searchTerm);
    this.validateResultCount(1);
    cy.intercept({
        url: this.expectedItemLoadCallUrl,
        method: 'GET'
      }
    ).as('itemCall')
      .get(this.resultsSelector)
      .get('button').contains(searchTerm)
      .click();
    targetDialogClazz.validateDialogOpened();
    cy.wait('@itemCall');
    return targetDialogClazz;
  }

  validateResultCount(expectedResults: number): void {
    cy.get(this.resultsSelector).find('button').should('have.length', expectedResults);
  }
}
