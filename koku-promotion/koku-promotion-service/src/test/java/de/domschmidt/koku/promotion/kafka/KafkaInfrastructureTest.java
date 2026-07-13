package de.domschmidt.koku.promotion.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.promotion.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import de.domschmidt.koku.promotion.kafka.promotion.config.KafkaPromotionConfig;
import de.domschmidt.koku.promotion.kafka.promotion.service.PromotionKafkaMaintenanceService;
import de.domschmidt.koku.promotion.kafka.promotion.service.PromotionKafkaService;
import de.domschmidt.koku.promotion.persistence.Promotion;
import de.domschmidt.koku.promotion.persistence.PromotionRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaInfrastructureTest {

    @Test
    void producerConfigurationAndServicePreservePromotionIdentity() throws Exception {
        assertThat(new KafkaConfiguration().getBootstrapAddress()).isNull();
        final KafkaConfiguration configuration = mock(KafkaConfiguration.class);
        when(configuration.getBootstrapAddress()).thenReturn("broker:9092");
        final KafkaPromotionConfig config = new KafkaPromotionConfig(configuration);
        assertThat(((DefaultKafkaProducerFactory<?, ?>) config.promotionKafkaDtoProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(config.promotionKafkaTemplate()).isNotNull();

        @SuppressWarnings("unchecked")
        final KafkaTemplate<Long, PromotionKafkaDto> template = mock(KafkaTemplate.class);
        @SuppressWarnings("unchecked")
        final SendResult<Long, PromotionKafkaDto> result = mock(SendResult.class);
        when(template.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(result));
        final Promotion promotion = new Promotion();
        promotion.setId(31L);
        promotion.setName("Summer");

        assertThat(new PromotionKafkaService(template).sendPromotion(promotion)).isSameAs(result);
        final ArgumentCaptor<PromotionKafkaDto> dto = ArgumentCaptor.forClass(PromotionKafkaDto.class);
        verify(template).send(eq(PromotionKafkaDto.TOPIC), eq(31L), dto.capture());
        assertThat(dto.getValue().getName()).isEqualTo("Summer");
    }

    @Test
    void maintenancePublishesAndHandlesTimeoutsAndInterrupts() throws Exception {
        final PromotionRepository repository = mock(PromotionRepository.class);
        final PromotionKafkaService service = mock(PromotionKafkaService.class);
        final Promotion promotion = new Promotion();
        when(repository.findAll()).thenReturn(List.of(promotion));

        new PromotionKafkaMaintenanceService(repository, service).onApplicationEvent(null);
        verify(service).sendPromotion(promotion);

        doThrow(new TimeoutException("broker")).when(service).sendPromotion(promotion);
        new PromotionKafkaMaintenanceService(repository, service).onApplicationEvent(null);
        doThrow(new InterruptedException("stopped")).when(service).sendPromotion(promotion);
        new PromotionKafkaMaintenanceService(repository, service).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();
    }
}
