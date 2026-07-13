package de.domschmidt.koku.customer.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.kafka.users.service.UserKTableProcessor;
import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

class KTableProcessorTest {

    @Test
    void processorsBuildMaterializedTables() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final StreamsBuilder builder = new StreamsBuilder();

        assertThat(new ActivityKTableProcessor(factory).activityKTable(builder)).isNotNull();
        assertThat(new ActivityStepKTableProcessor(factory).activityStepKTable(builder))
                .isNotNull();
        assertThat(new ProductKTableProcessor(factory).productKTable(builder)).isNotNull();
        assertThat(new ProductManufacturerKTableProcessor(factory).productManufacturerKTable(builder))
                .isNotNull();
        assertThat(new PromotionKTableProcessor(factory).promotionKTable(builder))
                .isNotNull();
        assertThat(new UserKTableProcessor(factory).userKTable(builder)).isNotNull();
        assertThat(builder.build().describe().subtopologies()).isNotEmpty();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void processorsExposeRunningKafkaStores() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.store(any())).thenReturn(store);

        assertThat(new ActivityKTableProcessor(factory).getActivities()).isSameAs(store);
        assertThat(new ActivityStepKTableProcessor(factory).getActivitySteps()).isSameAs(store);
        assertThat(new ProductKTableProcessor(factory).getProducts()).isSameAs(store);
        assertThat(new ProductManufacturerKTableProcessor(factory).getProductManufacturers())
                .isSameAs(store);
        assertThat(new PromotionKTableProcessor(factory).getPromotions()).isSameAs(store);
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
    void processorsRejectAccessBeforeKafkaStreamsStart() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final ActivityKTableProcessor activities = new ActivityKTableProcessor(factory);
        final ActivityStepKTableProcessor activitySteps = new ActivityStepKTableProcessor(factory);
        final ProductKTableProcessor products = new ProductKTableProcessor(factory);
        final ProductManufacturerKTableProcessor manufacturers = new ProductManufacturerKTableProcessor(factory);
        final PromotionKTableProcessor promotions = new PromotionKTableProcessor(factory);
        final UserKTableProcessor users = new UserKTableProcessor(factory);

        assertThatThrownBy(activities::getActivities).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(activitySteps::getActivitySteps).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(products::getProducts).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(manufacturers::getProductManufacturers).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(promotions::getPromotions).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(users::getUsers).isInstanceOf(IllegalStateException.class);
    }
}
