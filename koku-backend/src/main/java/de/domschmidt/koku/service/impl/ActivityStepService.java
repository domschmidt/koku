package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.ActivityStepRepository;
import de.domschmidt.koku.persistence.model.ActivityStep;
import de.domschmidt.koku.service.IActivityStepService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ActivityStepSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityStepService extends AbstractService<ActivityStep, ActivityStepSearchOptions> implements IActivityStepService {

    private final ActivityStepRepository activityStepRepository;

    public ActivityStepService(final ActivityStepRepository activityStepRepository) {
        this.activityStepRepository = activityStepRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<ActivityStep, Long> getDao() {
        return this.activityStepRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<ActivityStep> findAll(final ActivityStepSearchOptions customerSearchOptions) {
        return this.activityStepRepository.findAllByDescriptionContainingIgnoreCaseAndDeletedIsFalseOrderByDescriptionAsc(customerSearchOptions.getSearch());
    }

}
