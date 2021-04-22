package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.Activity;
import de.domschmidt.koku.service.searchoptions.ActivitySearchOptions;

public interface IActivityService extends IOperations<Activity, ActivitySearchOptions> {

}