package de.domschmidt.koku.dav.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dav.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.dav.kafka.customers.service.CustomerAppointmentKTableProcessor;
import de.domschmidt.koku.dav.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.dav.kafka.users.service.UserAppointmentKTableProcessor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

class KTableProcessorTest {

    @Test
    void processorsBuildTheirMaterializedTables() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final StreamsBuilder builder = new StreamsBuilder();

        assertThat(new CustomerKTableProcessor(factory).customerKTable(builder)).isNotNull();
        assertThat(new CustomerAppointmentKTableProcessor(factory).customerAppointmentKTable(builder))
                .isNotNull();
        assertThat(new UserAppointmentKTableProcessor(factory).userAppointmentKTable(builder))
                .isNotNull();
        assertThat(builder.build().describe().subtopologies()).isNotEmpty();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void processorsExposeStoresFromRunningKafkaStreams() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.store(any())).thenReturn(store);

        assertThat(new CustomerKTableProcessor(factory).getCustomers()).isSameAs(store);
        assertThat(new CustomerAppointmentKTableProcessor(factory).getCustomerAppointments())
                .isSameAs(store);
        assertThat(new UserAppointmentKTableProcessor(factory).getUserAppointments())
                .isSameAs(store);
    }

    @Test
    void processorsRejectStoreAccessBeforeKafkaStreamsStart() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final CustomerKTableProcessor customers = new CustomerKTableProcessor(factory);
        final CustomerAppointmentKTableProcessor customerAppointments = new CustomerAppointmentKTableProcessor(factory);
        final UserAppointmentKTableProcessor userAppointments = new UserAppointmentKTableProcessor(factory);

        assertThatThrownBy(customers::getCustomers)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka Streams are not started");
        assertThatThrownBy(customerAppointments::getCustomerAppointments)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka Streams are not started");
        assertThatThrownBy(userAppointments::getUserAppointments)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka Streams are not started");
    }

    @Test
    void kafkaConfigurationUsesApplicationAndBootstrapSettings() {
        final KafkaConfiguration configuration = new KafkaConfiguration();
        ReflectionTestUtils.setField(configuration, "applicationName", "dav");
        ReflectionTestUtils.setField(configuration, "bootstrapServers", "broker:9092");

        assertThat(configuration.kafkaStreamsConfiguration().asProperties())
                .containsEntry("application.id", "dav-streams")
                .containsEntry("bootstrap.servers", "broker:9092");
    }
}
