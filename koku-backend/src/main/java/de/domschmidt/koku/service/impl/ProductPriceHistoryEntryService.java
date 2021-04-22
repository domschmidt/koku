package de.domschmidt.koku.service.impl;

import com.google.common.collect.Lists;
import de.domschmidt.koku.persistence.dao.ProductPriceHistoryRepository;
import de.domschmidt.koku.persistence.model.ProductPriceHistoryEntry;
import de.domschmidt.koku.service.IProductPriceHistoryEntryService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ProductPriceHistoryEntrySearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductPriceHistoryEntryService extends AbstractService<ProductPriceHistoryEntry, ProductPriceHistoryEntrySearchOptions> implements IProductPriceHistoryEntryService {

    private final ProductPriceHistoryRepository productPriceHistoryRepository;

    public ProductPriceHistoryEntryService(final ProductPriceHistoryRepository productPriceHistoryRepository) {
        this.productPriceHistoryRepository = productPriceHistoryRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<ProductPriceHistoryEntry, Long> getDao() {
        return this.productPriceHistoryRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<ProductPriceHistoryEntry> findAll(final ProductPriceHistoryEntrySearchOptions productPriceHistoryEntrySearchOptions) {
        return Lists.newArrayList(getDao().findAll());
    }

}
