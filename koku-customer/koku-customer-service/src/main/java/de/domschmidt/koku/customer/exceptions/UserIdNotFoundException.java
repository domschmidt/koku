package de.domschmidt.koku.customer.exceptions;

public class UserIdNotFoundException extends Exception {

    public UserIdNotFoundException(String userId) {
        super("User with id " + userId + " not found");
    }

}
