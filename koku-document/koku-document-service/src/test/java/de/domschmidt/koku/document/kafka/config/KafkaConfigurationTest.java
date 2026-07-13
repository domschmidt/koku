package de.domschmidt.koku.document.kafka.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KafkaConfigurationTest {

    @Test
    void exposesConfiguredBootstrapAddress() {
        assertThat(new KafkaConfiguration().getBootstrapAddress()).isNull();
    }
}
