package de.domschmidt.koku.customer.kafka.promotions.service;

import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDtoSerdes;
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
public class PromotionKTableProcessor {

    public static final String STORE_NAME = "promotion-store";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public PromotionKTableProcessor(final StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, PromotionKafkaDto> promotionKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(
                        PromotionKafkaDto.TOPIC, Consumed.with(Serdes.Long(), new PromotionKafkaDtoSerdes()))
                .toTable(Materialized.<Long, PromotionKafkaDto>as(
                                Stores.inMemoryKeyValueStore(PromotionKTableProcessor.STORE_NAME))
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(new PromotionKafkaDtoSerdes()));
    }

    public ReadOnlyKeyValueStore<Long, PromotionKafkaDto> getPromotions() {
        return factoryBean
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        PromotionKTableProcessor.STORE_NAME,
                        QueryableStoreTypes.<Long, PromotionKafkaDto>keyValueStore()));
    }
}
