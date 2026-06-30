package de.domschmidt.koku.customer.kafka.users.service;

import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserKafkaDtoSerdes;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class UserKTableProcessor {

    public static final String STORE_NAME = "users-store";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public UserKTableProcessor(final StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<String, UserKafkaDto> userKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(UserKafkaDto.TOPIC, Consumed.with(Serdes.String(), new UserKafkaDtoSerdes()))
                .toTable(Materialized.<String, UserKafkaDto>as(
                                Stores.inMemoryKeyValueStore(UserKTableProcessor.STORE_NAME))
                        .withKeySerde(Serdes.String())
                        .withValueSerde(new UserKafkaDtoSerdes()));
    }

    public Map<String, UserKafkaDto> getUsers() {
        final Map<String, UserKafkaDto> result = new HashMap<>();
        try (KeyValueIterator<String, UserKafkaDto> userStore = getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        UserKTableProcessor.STORE_NAME, QueryableStoreTypes.<String, UserKafkaDto>keyValueStore()))
                .all()) {
            while (userStore.hasNext()) {
                final KeyValue<String, UserKafkaDto> currentUser = userStore.next();
                result.put(currentUser.key, currentUser.value);
            }
        }
        return result;
    }

    private KafkaStreams getKafkaStreams() {
        final KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new IllegalStateException("Kafka Streams are not started");
        }
        return kafkaStreams;
    }
}
