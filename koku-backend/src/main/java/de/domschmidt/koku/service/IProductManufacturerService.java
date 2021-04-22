package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.ProductManufacturer;
import de.domschmidt.koku.service.searchoptions.ProductManufacturerSearchOptions;

public interface IProductManufacturerService extends IOperations<ProductManufacturer, ProductManufacturerSearchOptions> {

}