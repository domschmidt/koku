package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.ProductRepository;
import de.domschmidt.koku.persistence.model.Product;
import de.domschmidt.koku.service.IProductService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ProductSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService extends AbstractService<Product, ProductSearchOptions> implements IProductService {

    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<Product, Long> getDao() {
        return this.productRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll(final ProductSearchOptions productSearchOptions) {
        return this.productRepository.findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseOrderByDescriptionAsc(productSearchOptions.getSearch());
    }
}
