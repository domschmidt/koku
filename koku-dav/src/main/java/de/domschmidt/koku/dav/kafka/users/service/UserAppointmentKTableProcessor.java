package de.domschmidt.koku.dav.kafka.users.service;

import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDtoSerdes;
import org.apache.kafka.common.serialization.Serdes;
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
public class UserAppointmentKTableProcessor {

    public static final String STORE_NAME = "user-appointment-store";

    private final StreamsBuilderFactoryBean factoryBean;

    public UserAppointmentKTableProcessor(final StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Bean
    public KTable<Long, UserAppointmentKafkaDto> userAppointmentKTable(final StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(
                        UserAppointmentKafkaDto.TOPIC,
                        Consumed.with(Serdes.Long(), new UserAppointmentKafkaDtoSerdes()))
                .toTable(Materialized.<Long, UserAppointmentKafkaDto>as(Stores.inMemoryKeyValueStore(STORE_NAME))
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(new UserAppointmentKafkaDtoSerdes()));
    }

    public ReadOnlyKeyValueStore<Long, UserAppointmentKafkaDto> getUserAppointments() {
        return factoryBean
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        STORE_NAME, QueryableStoreTypes.<Long, UserAppointmentKafkaDto>keyValueStore()));
    }
}
