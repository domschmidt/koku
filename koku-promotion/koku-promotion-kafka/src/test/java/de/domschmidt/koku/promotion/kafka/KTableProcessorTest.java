package de.domschmidt.koku.promotion.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.promotion.kafka.promotions.service.PromotionKTableProcessor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

class KTableProcessorTest {

    @Test
    void processorsBuildTheirMaterializedTables() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final StreamsBuilder builder = new StreamsBuilder();

        assertThat(new PromotionKTableProcessor(factory).promotionKTable(builder))
                .isNotNull();
        assertThat(builder.build().describe().subtopologies()).isNotEmpty();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void processorsExposeTheirStores() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.store(any())).thenReturn(store);

        assertThat(new PromotionKTableProcessor(factory).getPromotions()).isSameAs(store);
    }

    @Test
    void processorsRejectAccessBeforeKafkaStreamsStart() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);

        assertThatThrownBy(() -> new PromotionKTableProcessor(factory).getPromotions())
                .isInstanceOf(IllegalStateException.class);
    }
}
