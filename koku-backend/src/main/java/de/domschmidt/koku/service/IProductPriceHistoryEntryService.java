package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.ProductPriceHistoryEntry;
import de.domschmidt.koku.service.searchoptions.ProductPriceHistoryEntrySearchOptions;

public interface IProductPriceHistoryEntryService extends IOperations<ProductPriceHistoryEntry, ProductPriceHistoryEntrySearchOptions> {

}