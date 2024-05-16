package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.ActivityRepository;
import de.domschmidt.koku.persistence.model.Activity;
import de.domschmidt.koku.service.IActivityService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ActivitySearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityService extends AbstractService<Activity, ActivitySearchOptions> implements IActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(final ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<Activity, Long> getDao() {
        return this.activityRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<Activity> findAll(final ActivitySearchOptions activitySearchOptions) {
        final List<Activity> result;
        if (activitySearchOptions.isHavingRelevanceForPriceListOnly()) {
            result = this.activityRepository.findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseAndRelevantForPriceListIsTrueOrderByDescriptionAsc(activitySearchOptions.getSearch());
        } else {
            result = this.activityRepository.findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseOrderByDescriptionAsc(activitySearchOptions.getSearch());
        }
        return result;
    }

}
