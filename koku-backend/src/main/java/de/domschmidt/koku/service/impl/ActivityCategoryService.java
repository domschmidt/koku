package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.ActivityCategoryRepository;
import de.domschmidt.koku.persistence.model.ActivityCategory;
import de.domschmidt.koku.service.IActivityCategoryService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ActivityCategorySearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityCategoryService extends AbstractService<ActivityCategory, ActivityCategorySearchOptions> implements IActivityCategoryService {

    private final ActivityCategoryRepository activityCategoryRepository;

    public ActivityCategoryService(final ActivityCategoryRepository activityCategoryRepository) {
        this.activityCategoryRepository = activityCategoryRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<ActivityCategory, Long> getDao() {
        return this.activityCategoryRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<ActivityCategory> findAll(final ActivityCategorySearchOptions searchOptions) {
        return this.activityCategoryRepository.findAllByDescriptionContainingIgnoreCaseOrderByDescriptionAsc(searchOptions.getSearch());
    }

}
