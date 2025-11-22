package de.domschmidt.koku.customer.exceptions;

public class ActivityStepIdNotFoundException extends Exception {
    public ActivityStepIdNotFoundException(Long activityStepId) {
        super(String.format("ActivityStepId %s not found", activityStepId));
    }
}
