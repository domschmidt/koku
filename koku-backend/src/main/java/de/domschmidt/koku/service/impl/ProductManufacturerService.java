package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.ProductManufacturerRepository;
import de.domschmidt.koku.persistence.model.ProductManufacturer;
import de.domschmidt.koku.service.IProductManufacturerService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ProductManufacturerSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductManufacturerService extends AbstractService<ProductManufacturer, ProductManufacturerSearchOptions> implements IProductManufacturerService {

    private final ProductManufacturerRepository productManufacturerRepository;

    public ProductManufacturerService(final ProductManufacturerRepository productManufacturerRepository) {
        this.productManufacturerRepository = productManufacturerRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<ProductManufacturer, Long> getDao() {
        return this.productManufacturerRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<ProductManufacturer> findAll(final ProductManufacturerSearchOptions productSearchOptions) {
        return this.productManufacturerRepository.findAllByNameContainingIgnoreCaseAndDeletedIsFalseOrderByNameAsc(productSearchOptions.getSearch());
    }

}
