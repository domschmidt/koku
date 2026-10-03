package de.domschmidt.koku.promotion.kafka.productmanufacturers.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

class ProductManufacturerKTableProcessorTest {

    @Test
    void failsFastWhenKafkaStreamsAreNotStarted() {
        final StreamsBuilderFactoryBean factoryBean = mock(StreamsBuilderFactoryBean.class);
        when(factoryBean.getKafkaStreams()).thenReturn(null);

        assertThatThrownBy(() -> new ProductManufacturerKTableProcessor(factoryBean).getProductManufacturers())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not started");
    }

    @Test
    void queriesTheManufacturerStore() {
        final StreamsBuilderFactoryBean factoryBean = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams kafkaStreams = mock(KafkaStreams.class);
        @SuppressWarnings("unchecked")
        final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> store = mock(ReadOnlyKeyValueStore.class);
        when(factoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        doReturn(store).when(kafkaStreams).store(any(StoreQueryParameters.class));

        assertThat(new ProductManufacturerKTableProcessor(factoryBean).getProductManufacturers())
                .isSameAs(store);
    }

    @Test
    @SuppressWarnings("unchecked")
    void materializesTheManufacturerTableFromTheTopic() {
        final StreamsBuilder streamsBuilder = mock(StreamsBuilder.class);
        final KStream<Long, ProductManufacturerKafkaDto> stream = mock(KStream.class);
        final KTable<Long, ProductManufacturerKafkaDto> table = mock(KTable.class);
        when(streamsBuilder.stream(eq(ProductManufacturerKafkaDto.TOPIC), any(Consumed.class)))
                .thenReturn(stream);
        when(stream.toTable(any(Materialized.class))).thenReturn(table);

        assertThat(new ProductManufacturerKTableProcessor(mock(StreamsBuilderFactoryBean.class))
                        .productManufacturerKTable(streamsBuilder))
                .isSameAs(table);
    }
}
