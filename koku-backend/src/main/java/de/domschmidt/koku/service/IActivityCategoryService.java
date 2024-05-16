package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.ActivityCategory;
import de.domschmidt.koku.service.searchoptions.ActivityCategorySearchOptions;

public interface IActivityCategoryService extends IOperations<ActivityCategory, ActivityCategorySearchOptions> {

}
