package de.domschmidt.koku.file.kafka.customers.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDtoSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class CustomerKTableProcessor {

    public static final String STORE_NAME = "customer-store";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public CustomerKTableProcessor(
            final StreamsBuilderFactoryBean factoryBean
    ) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, CustomerKafkaDto> customerKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(CustomerKafkaDto.TOPIC,
                Consumed.with(Serdes.Long(), new CustomerKafkaDtoSerdes())
        ).toTable(Materialized.<Long, CustomerKafkaDto>as(Stores.inMemoryKeyValueStore(CustomerKTableProcessor.STORE_NAME))
                .withKeySerde(Serdes.Long())
                .withValueSerde(new CustomerKafkaDtoSerdes())
        );
    }

    public ReadOnlyKeyValueStore<Long, CustomerKafkaDto> getCustomers() {
        return factoryBean.getKafkaStreams().store(
                StoreQueryParameters.fromNameAndType(CustomerKTableProcessor.STORE_NAME,
                        QueryableStoreTypes.<Long, CustomerKafkaDto>keyValueStore())
        );
    }

}