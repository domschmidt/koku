package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.Sale;
import de.domschmidt.koku.service.searchoptions.SaleSearchOptions;

public interface ISaleService extends IOperations<Sale, SaleSearchOptions> {

}