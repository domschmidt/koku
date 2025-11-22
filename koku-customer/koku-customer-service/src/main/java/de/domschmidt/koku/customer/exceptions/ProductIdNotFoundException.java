package de.domschmidt.koku.customer.exceptions;

public class ProductIdNotFoundException extends Exception {
    public ProductIdNotFoundException(Long productId) {
        super("Product Id " + productId + " not found");
    }
}
