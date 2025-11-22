package de.domschmidt.koku.customer.exceptions;

public class ActivityIdNotFoundException extends Exception {
    public ActivityIdNotFoundException(Long activityId) {
        super("Activity Id " + activityId + " not found");
    }
}
