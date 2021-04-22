package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.PromotionRepository;
import de.domschmidt.koku.persistence.model.Promotion;
import de.domschmidt.koku.service.IPromotionService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.PromotionSearchOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PromotionService extends AbstractService<Promotion, PromotionSearchOptions> implements IPromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(final PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<Promotion, Long> getDao() {
        return this.promotionRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> findAll(final PromotionSearchOptions promotionSearchOptions) {
        final String search = StringUtils.isNotBlank(promotionSearchOptions.getSearch()) ? promotionSearchOptions.getSearch() : "";
        final boolean activeOnly = Boolean.TRUE.equals(promotionSearchOptions.getActiveOnly());
        if (activeOnly) {
            final LocalDate now = LocalDate.now();
            return this.promotionRepository.findAllByNameLikeIgnoreCaseAndDeletedIsFalseAndStartDateBeforeOrEqualToAndEndDateAfterOrEqualTo('%' + search + '%', now);
        } else {
            return this.promotionRepository.findAllByNameContainingIgnoreCaseAndDeletedIsFalseOrderByNameAsc(search);
        }
    }

}
