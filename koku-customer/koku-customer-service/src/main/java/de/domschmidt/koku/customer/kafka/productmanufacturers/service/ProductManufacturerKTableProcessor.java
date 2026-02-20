package de.domschmidt.koku.customer.kafka.productmanufacturers.service;

import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDtoSerdes;
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
public class ProductManufacturerKTableProcessor {

    public static final String STORE_NAME = "product-manufacturer-store";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public ProductManufacturerKTableProcessor(final StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, ProductManufacturerKafkaDto> productManufacturerKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(
                        ProductManufacturerKafkaDto.TOPIC,
                        Consumed.with(Serdes.Long(), new ProductManufacturerKafkaDtoSerdes()))
                .toTable(Materialized.<Long, ProductManufacturerKafkaDto>as(
                                Stores.inMemoryKeyValueStore(ProductManufacturerKTableProcessor.STORE_NAME))
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(new ProductManufacturerKafkaDtoSerdes()));
    }

    public ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> getProductManufacturers() {
        return factoryBean
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        ProductManufacturerKTableProcessor.STORE_NAME,
                        QueryableStoreTypes.<Long, ProductManufacturerKafkaDto>keyValueStore()));
    }
}
