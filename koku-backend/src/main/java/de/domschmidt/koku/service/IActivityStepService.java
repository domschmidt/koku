package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.ActivityStep;
import de.domschmidt.koku.service.searchoptions.ActivityStepSearchOptions;

public interface IActivityStepService extends IOperations<ActivityStep, ActivityStepSearchOptions> {

}