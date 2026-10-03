package de.domschmidt.koku.promotion.exceptions;

public class ManufacturerIdNotFoundException extends Exception {
    public ManufacturerIdNotFoundException(Long manufacturerId) {
        super("Manufacturer Id " + manufacturerId + " not found");
    }
}
