package de.domschmidt.koku.dav.kafka.customers.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDtoSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class CustomerAppointmentKTableProcessor {

    public static final String STORE_NAME = "customer-appointment-store";

    private final StreamsBuilderFactoryBean factoryBean;

    public CustomerAppointmentKTableProcessor(final StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, CustomerAppointmentKafkaDto> customerAppointmentKTable(final StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(
                        CustomerAppointmentKafkaDto.TOPIC,
                        Consumed.with(Serdes.Long(), new CustomerAppointmentKafkaDtoSerdes()))
                .toTable(Materialized.<Long, CustomerAppointmentKafkaDto>as(
                                Stores.inMemoryKeyValueStore(CustomerAppointmentKTableProcessor.STORE_NAME))
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(new CustomerAppointmentKafkaDtoSerdes()));
    }

    public ReadOnlyKeyValueStore<Long, CustomerAppointmentKafkaDto> getCustomerAppointments() {
        return getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        CustomerAppointmentKTableProcessor.STORE_NAME,
                        QueryableStoreTypes.<Long, CustomerAppointmentKafkaDto>keyValueStore()));
    }

    private KafkaStreams getKafkaStreams() {
        final KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new IllegalStateException("Kafka Streams are not started");
        }
        return kafkaStreams;
    }
}
