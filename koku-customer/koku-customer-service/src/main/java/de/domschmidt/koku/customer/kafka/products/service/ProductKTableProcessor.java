package de.domschmidt.koku.customer.kafka.products.service;

import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDtoSerdes;
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
public class ProductKTableProcessor {

    public static final String STORE_NAME = "product-store";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public ProductKTableProcessor(final StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, ProductKafkaDto> productKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(ProductKafkaDto.TOPIC, Consumed.with(Serdes.Long(), new ProductKafkaDtoSerdes()))
                .toTable(Materialized.<Long, ProductKafkaDto>as(
                                Stores.inMemoryKeyValueStore(ProductKTableProcessor.STORE_NAME))
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(new ProductKafkaDtoSerdes()));
    }

    public ReadOnlyKeyValueStore<Long, ProductKafkaDto> getProducts() {
        return factoryBean
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        ProductKTableProcessor.STORE_NAME, QueryableStoreTypes.<Long, ProductKafkaDto>keyValueStore()));
    }
}
