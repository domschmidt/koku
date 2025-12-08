package de.domschmidt.koku.customer.kafka.activity_steps.service;

import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDtoSerdes;
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
public class ActivityStepKTableProcessor {

    public static final String STORE_NAME = "activity-step-store";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public ActivityStepKTableProcessor(
            final StreamsBuilderFactoryBean factoryBean
    ) {
        this.factoryBean = factoryBean;
    }
    
    @Bean
    public KTable<Long, ActivityStepKafkaDto> activityStepKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.stream(ActivityStepKafkaDto.TOPIC,
                Consumed.with(Serdes.Long(), new ActivityStepKafkaDtoSerdes())
        ).toTable(Materialized.<Long, ActivityStepKafkaDto>as(Stores.inMemoryKeyValueStore(ActivityStepKTableProcessor.STORE_NAME))
                .withKeySerde(Serdes.Long())
                .withValueSerde(new ActivityStepKafkaDtoSerdes())
        );
    }

    public ReadOnlyKeyValueStore<Long, ActivityStepKafkaDto> getActivitySteps() {
        return factoryBean.getKafkaStreams().store(
                StoreQueryParameters.fromNameAndType(ActivityStepKTableProcessor.STORE_NAME,
                        QueryableStoreTypes.<Long, ActivityStepKafkaDto>keyValueStore())
        );
    }


}