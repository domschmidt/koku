package de.domschmidt.koku.service.impl;

import com.google.common.collect.Lists;
import de.domschmidt.koku.persistence.dao.ActivityPriceHistoryRepository;
import de.domschmidt.koku.persistence.model.ActivityPriceHistoryEntry;
import de.domschmidt.koku.service.IActivityPriceHistoryEntryService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.ActivityPriceHistoryEntrySearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActivityPriceHistoryEntryService extends AbstractService<ActivityPriceHistoryEntry, ActivityPriceHistoryEntrySearchOptions> implements IActivityPriceHistoryEntryService {

    private final ActivityPriceHistoryRepository activityPriceHistoryRepository;

    public ActivityPriceHistoryEntryService(final ActivityPriceHistoryRepository activityPriceHistoryRepository) {
        this.activityPriceHistoryRepository = activityPriceHistoryRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<ActivityPriceHistoryEntry, Long> getDao() {
        return this.activityPriceHistoryRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<ActivityPriceHistoryEntry> findAll(final ActivityPriceHistoryEntrySearchOptions activityPriceHistoryEntrySearchOptions) {
        return Lists.newArrayList(getDao().findAll());
    }

}
