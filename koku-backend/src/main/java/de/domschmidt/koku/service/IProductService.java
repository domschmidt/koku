package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.Product;
import de.domschmidt.koku.service.searchoptions.ProductSearchOptions;

public interface IProductService extends IOperations<Product, ProductSearchOptions> {

}