package de.domschmidt.koku.product.kafka.util;

import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;

/**
 * Formats product names for overviews and references. Milliliters are appended to the name so that
 * lists, summaries and references display e.g. {@code Shampoo (250 ml)}.
 */
public final class ProductDisplayNameFormatter {

    private ProductDisplayNameFormatter() {}

    public static String withMilliliters(final String name, final Integer milliliters) {
        if (milliliters == null || milliliters <= 0) {
            return name;
        }
        if (name == null || name.isBlank()) {
            return milliliters + " ml";
        }
        return name + " (" + milliliters + " ml)";
    }

    public static String withMilliliters(final ProductKafkaDto product) {
        if (product == null) {
            return null;
        }
        return withMilliliters(product.getName(), product.getMilliliters());
    }
}
