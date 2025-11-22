package de.domschmidt.koku.customer.kafka.activity_steps.service;

import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDtoSerdes;
import org.apache.kafka.common.serialization.Serdes;
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

import java.util.HashMap;
import java.util.Map;

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

    public Map<Long, ActivityStepKafkaDto> getActivitySteps() {
        final Map<Long, ActivityStepKafkaDto> result = new HashMap<>();
        final KeyValueIterator<Long, ActivityStepKafkaDto> activityStepStore = factoryBean.getKafkaStreams().store(
                StoreQueryParameters.fromNameAndType(ActivityStepKTableProcessor.STORE_NAME,
                        QueryableStoreTypes.<Long, ActivityStepKafkaDto>keyValueStore())
        ).all();
        while (activityStepStore.hasNext()) {
            final KeyValue<Long, ActivityStepKafkaDto> currentActivityStep = activityStepStore.next();
            result.put(currentActivityStep.key, currentActivityStep.value);
        }
        return result;
    }


}