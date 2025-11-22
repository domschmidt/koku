package de.domschmidt.koku.product.exceptions;

public class ManufacturerIdNotFoundException extends Exception {
    public ManufacturerIdNotFoundException(Long manufacturerId) {
        super("Manufacturer " + manufacturerId + " not found");
    }
}
