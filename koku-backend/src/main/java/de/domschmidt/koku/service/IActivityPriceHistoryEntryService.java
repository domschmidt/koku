package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.ActivityPriceHistoryEntry;
import de.domschmidt.koku.service.searchoptions.ActivityPriceHistoryEntrySearchOptions;

public interface IActivityPriceHistoryEntryService extends IOperations<ActivityPriceHistoryEntry, ActivityPriceHistoryEntrySearchOptions> {

}