package de.domschmidt.koku.user.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import de.domschmidt.koku.user.kafka.users.service.UserAppointmentKTableProcessor;
import de.domschmidt.koku.user.kafka.users.service.UserKTableProcessor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

class KTableProcessorTest {

    @Test
    void processorsBuildTheirMaterializedTables() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final StreamsBuilder builder = new StreamsBuilder();

        assertThat(new UserKTableProcessor(factory).userKTable(builder)).isNotNull();
        assertThat(new UserAppointmentKTableProcessor(factory).userAppointmentKTable(builder))
                .isNotNull();
        assertThat(builder.build().describe().subtopologies()).isNotEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void userProcessorCollectsEveryStoreEntry() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        final ReadOnlyKeyValueStore<String, UserKafkaDto> store = mock(ReadOnlyKeyValueStore.class);
        final KeyValueIterator<String, UserKafkaDto> iterator = mock(KeyValueIterator.class);
        final UserKafkaDto user = UserKafkaDto.builder().id("u-1").build();
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.store(any())).thenReturn(store);
        when(store.all()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(KeyValue.pair("u-1", user));

        assertThat(new UserKTableProcessor(factory).getUsers()).containsEntry("u-1", user);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void appointmentProcessorExposesItsStore() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.store(any())).thenReturn(store);

        assertThat(new UserAppointmentKTableProcessor(factory).getUserAppointments())
                .isSameAs(store);
    }

    @Test
    void processorsRejectAccessBeforeKafkaStreamsStart() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);

        assertThatThrownBy(() -> new UserKTableProcessor(factory).getUsers()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new UserAppointmentKTableProcessor(factory).getUserAppointments())
                .isInstanceOf(IllegalStateException.class);
    }
}
