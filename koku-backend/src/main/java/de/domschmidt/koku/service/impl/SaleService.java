package de.domschmidt.koku.service.impl;

import com.google.common.collect.Lists;
import de.domschmidt.koku.persistence.dao.SaleRepository;
import de.domschmidt.koku.persistence.model.Sale;
import de.domschmidt.koku.service.ISaleService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.SaleSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SaleService extends AbstractService<Sale, SaleSearchOptions> implements ISaleService {

    private final SaleRepository saleRepository;

    public SaleService(final SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<Sale, Long> getDao() {
        return this.saleRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<Sale> findAll(final SaleSearchOptions saleSearchOptions) {
        return Lists.newArrayList(getDao().findAll());
    }

}
