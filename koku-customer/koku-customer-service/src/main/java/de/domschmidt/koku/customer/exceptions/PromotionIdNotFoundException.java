package de.domschmidt.koku.customer.exceptions;

public class PromotionIdNotFoundException extends Exception {
    public PromotionIdNotFoundException(Long promotionId) {
        super("Promotion id " + promotionId + " not found");
    }
}
