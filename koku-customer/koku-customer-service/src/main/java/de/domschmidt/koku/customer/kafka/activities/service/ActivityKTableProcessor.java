package de.domschmidt.koku.customer.kafka.activities.service;

import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDtoSerdes;
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
public class ActivityKTableProcessor {

    public static final String STORE_NAME = "activity-store";

    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public ActivityKTableProcessor(StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, ActivityKafkaDto> activityKTable(StreamsBuilder builder) {
        return builder
                .stream(ActivityKafkaDto.TOPIC, Consumed.with(Serdes.Long(), new ActivityKafkaDtoSerdes()))
                .toTable(Materialized
                        .<Long, ActivityKafkaDto>as(Stores.inMemoryKeyValueStore(STORE_NAME))
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(new ActivityKafkaDtoSerdes()));
    }

    public ReadOnlyKeyValueStore<Long, ActivityKafkaDto> getActivities() {
        return factoryBean.getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        STORE_NAME,
                        QueryableStoreTypes.keyValueStore())
                );
    }
}