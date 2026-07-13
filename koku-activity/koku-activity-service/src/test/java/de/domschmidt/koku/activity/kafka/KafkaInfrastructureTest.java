package de.domschmidt.koku.activity.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.activity.kafka.activity.config.KafkaActivityConfig;
import de.domschmidt.koku.activity.kafka.activity.config.KafkaActivityStepConfig;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityKafkaMaintenanceService;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityKafkaService;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityStepKafkaMaintenanceService;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityStepKafkaService;
import de.domschmidt.koku.activity.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto;
import de.domschmidt.koku.activity.persistence.Activity;
import de.domschmidt.koku.activity.persistence.ActivityRepository;
import de.domschmidt.koku.activity.persistence.ActivityStep;
import de.domschmidt.koku.activity.persistence.ActivityStepRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaInfrastructureTest {

    @Test
    void producerConfigurationsUseConfiguredBrokerAndCreateTemplates() {
        assertThat(new KafkaConfiguration().getBootstrapAddress()).isNull();
        final KafkaConfiguration configuration = mock(KafkaConfiguration.class);
        when(configuration.getBootstrapAddress()).thenReturn("broker:9092");
        final KafkaActivityConfig activityConfig = new KafkaActivityConfig(configuration);
        final KafkaActivityStepConfig stepConfig = new KafkaActivityStepConfig(configuration);

        assertThat(((DefaultKafkaProducerFactory<?, ?>) activityConfig.activityProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(((DefaultKafkaProducerFactory<?, ?>) stepConfig.activityStepKafkaDtoProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(activityConfig.activityKafkaTemplate()).isNotNull();
        assertThat(stepConfig.activityStepKafkaTemplate()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void servicesPublishTransformedEntitiesWithTheirIdentity() throws Exception {
        final KafkaTemplate<Long, ActivityKafkaDto> activityTemplate = mock(KafkaTemplate.class);
        final KafkaTemplate<Long, ActivityStepKafkaDto> stepTemplate = mock(KafkaTemplate.class);
        final SendResult<Long, ActivityKafkaDto> activityResult = mock(SendResult.class);
        final SendResult<Long, ActivityStepKafkaDto> stepResult = mock(SendResult.class);
        when(activityTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(activityResult));
        when(stepTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(stepResult));
        final Activity activity = new Activity();
        activity.setId(11L);
        activity.setName("Cut");
        final ActivityStep step = new ActivityStep();
        step.setId(12L);
        step.setName("Wash");

        assertThat(new ActivityKafkaService(activityTemplate).sendActivity(activity))
                .isSameAs(activityResult);
        assertThat(new ActivityStepKafkaService(stepTemplate).sendActivityStep(step))
                .isSameAs(stepResult);

        final ArgumentCaptor<ActivityKafkaDto> activityDto = ArgumentCaptor.forClass(ActivityKafkaDto.class);
        final ArgumentCaptor<ActivityStepKafkaDto> stepDto = ArgumentCaptor.forClass(ActivityStepKafkaDto.class);
        verify(activityTemplate).send(eq(ActivityKafkaDto.TOPIC), eq(11L), activityDto.capture());
        verify(stepTemplate).send(eq(ActivityStepKafkaDto.TOPIC), eq(12L), stepDto.capture());
        assertThat(activityDto.getValue().getName()).isEqualTo("Cut");
        assertThat(stepDto.getValue().getName()).isEqualTo("Wash");
    }

    @Test
    void maintenancePublishesAllEntities() throws Exception {
        final ActivityRepository activities = mock(ActivityRepository.class);
        final ActivityStepRepository steps = mock(ActivityStepRepository.class);
        final ActivityKafkaService activityService = mock(ActivityKafkaService.class);
        final ActivityStepKafkaService stepService = mock(ActivityStepKafkaService.class);
        final Activity activity = new Activity();
        final ActivityStep step = new ActivityStep();
        when(activities.findAll()).thenReturn(List.of(activity));
        when(steps.findAll()).thenReturn(List.of(step));

        new ActivityKafkaMaintenanceService(activities, activityService).onApplicationEvent(null);
        new ActivityStepKafkaMaintenanceService(steps, stepService).onApplicationEvent(null);

        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(activityService)
                .sendActivity(activity);
        doThrow(new InterruptedException("stopped")).when(stepService).sendActivityStep(step);
        new ActivityKafkaMaintenanceService(activities, activityService).onApplicationEvent(null);
        new ActivityStepKafkaMaintenanceService(steps, stepService).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();

        verify(activityService, atLeastOnce()).sendActivity(activity);
        verify(stepService, atLeastOnce()).sendActivityStep(step);
    }

    @Test
    void maintenanceHandlesInterruptAndBrokerFailures() throws Exception {
        final ActivityRepository activities = mock(ActivityRepository.class);
        final ActivityStepRepository steps = mock(ActivityStepRepository.class);
        final ActivityKafkaService activityService = mock(ActivityKafkaService.class);
        final ActivityStepKafkaService stepService = mock(ActivityStepKafkaService.class);
        final Activity activity = new Activity();
        final ActivityStep step = new ActivityStep();
        when(activities.findAll()).thenReturn(List.of(activity));
        when(steps.findAll()).thenReturn(List.of(step));
        doThrow(new InterruptedException("stopped")).when(activityService).sendActivity(activity);
        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(stepService)
                .sendActivityStep(step);

        new ActivityKafkaMaintenanceService(activities, activityService).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();
        new ActivityStepKafkaMaintenanceService(steps, stepService).onApplicationEvent(null);
    }
}
