package de.domschmidt.koku.product.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.util.ProductDisplayNameFormatter;
import org.junit.jupiter.api.Test;

class ProductDisplayNameFormatterTest {

    @Test
    void appendsMillilitersToName() {
        assertThat(ProductDisplayNameFormatter.withMilliliters("Shampoo", 250)).isEqualTo("Shampoo (250 ml)");
    }

    @Test
    void ignoresMissingOrNonPositiveMilliliters() {
        assertThat(ProductDisplayNameFormatter.withMilliliters("Shampoo", null)).isEqualTo("Shampoo");
        assertThat(ProductDisplayNameFormatter.withMilliliters("Shampoo", 0)).isEqualTo("Shampoo");
    }

    @Test
    void handlesMissingName() {
        assertThat(ProductDisplayNameFormatter.withMilliliters(null, 250)).isEqualTo("250 ml");
    }

    @Test
    void formatsKafkaDto() {
        assertThat(ProductDisplayNameFormatter.withMilliliters(ProductKafkaDto.builder()
                        .name("Shampoo")
                        .milliliters(250)
                        .build()))
                .isEqualTo("Shampoo (250 ml)");
    }
}
