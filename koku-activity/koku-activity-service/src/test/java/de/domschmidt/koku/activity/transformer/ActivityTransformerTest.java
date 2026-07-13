package de.domschmidt.koku.activity.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.activity.kafka.activity.transformer.ActivityStepToKafkaActivityStepDtoTransformer;
import de.domschmidt.koku.activity.kafka.activity.transformer.ActivityToKafkaActivityDtoTransformer;
import de.domschmidt.koku.activity.persistence.Activity;
import de.domschmidt.koku.activity.persistence.ActivityPriceHistoryEntry;
import de.domschmidt.koku.activity.persistence.ActivityStep;
import de.domschmidt.koku.dto.activity.KokuActivityDto;
import de.domschmidt.koku.dto.activity.KokuActivityStepDto;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ActivityTransformerTest {

    @Test
    void activityRoundTripMapsDurationPriceAndLifecycle() {
        final Activity activity = new Activity();
        activity.setId(1L);
        activity.setVersion(2L);
        activity.setName("Cut");
        activity.setApproximatelyDuration(Duration.ofMinutes(75));
        activity.getPriceHistory().add(new ActivityPriceHistoryEntry(activity, new BigDecimal("49.90")));

        final KokuActivityDto dto = new ActivityToActivityDtoTransformer().transformToDto(activity);

        assertThat(dto.getName()).isEqualTo("Cut");
        assertThat(dto.getApproximatelyDuration()).isEqualTo(LocalTime.of(1, 15));
        assertThat(dto.getPrice()).isEqualByComparingTo("49.90");
    }

    @Test
    void activityUpdateAddsOnlyChangedPriceAndPreservesAbsentFields() {
        final Activity activity = new Activity();
        activity.setName("Existing");
        activity.getPriceHistory().add(new ActivityPriceHistoryEntry(activity, new BigDecimal("10.00")));
        final ActivityToActivityDtoTransformer transformer = new ActivityToActivityDtoTransformer();

        transformer.transformToEntity(
                activity,
                KokuActivityDto.builder().price(new BigDecimal("10.00")).build());
        assertThat(activity.getPriceHistory()).hasSize(1);

        transformer.transformToEntity(
                activity,
                KokuActivityDto.builder()
                        .name("Updated")
                        .approximatelyDuration(LocalTime.of(0, 45))
                        .price(new BigDecimal("12.00"))
                        .deleted(true)
                        .build());
        assertThat(activity.getName()).isEqualTo("Updated");
        assertThat(activity.getApproximatelyDuration()).isEqualTo(Duration.ofMinutes(45));
        assertThat(activity.getPriceHistory()).hasSize(2);
        assertThat(activity.isDeleted()).isTrue();
    }

    @Test
    void emptyPriceHistoryMapsToNull() {
        assertThat(new ActivityToActivityDtoTransformer()
                        .transformToDto(new Activity())
                        .getPrice())
                .isNull();
    }

    @Test
    void activityStepRoundTripMapsAndUpdatesFields() {
        final ActivityStep step = new ActivityStep();
        step.setId(3L);
        step.setName("Wash");
        final ActivityStepToActivityStepDtoTransformer transformer = new ActivityStepToActivityStepDtoTransformer();

        assertThat(transformer.transformToDto(step).getName()).isEqualTo("Wash");
        transformer.transformToEntity(
                step, KokuActivityStepDto.builder().name("Dry").deleted(true).build());
        assertThat(step.getName()).isEqualTo("Dry");
        assertThat(step.isDeleted()).isTrue();
    }

    @Test
    void kafkaSnapshotsContainActivityAndStepBusinessFields() {
        final Activity activity = new Activity();
        activity.setId(1L);
        activity.setName("Cut");
        activity.setApproximatelyDuration(Duration.ofMinutes(30));
        activity.setDeleted(true);
        activity.getPriceHistory().add(new ActivityPriceHistoryEntry(activity, new BigDecimal("20.00")));

        final var activitySnapshot = new ActivityToKafkaActivityDtoTransformer().transformToDto(activity);

        assertThat(activitySnapshot.getId()).isEqualTo(1L);
        assertThat(activitySnapshot.getName()).isEqualTo("Cut");
        assertThat(activitySnapshot.getApproximatelyDuration()).isEqualTo(Duration.ofMinutes(30));
        assertThat(activitySnapshot.getPriceHistory())
                .singleElement()
                .extracting("price")
                .isEqualTo(new BigDecimal("20.00"));
        assertThat(activitySnapshot.getDeleted()).isTrue();

        final ActivityStep step = new ActivityStep();
        step.setId(2L);
        step.setName("Wash");
        step.setDeleted(true);
        final var stepSnapshot = new ActivityStepToKafkaActivityStepDtoTransformer().transformToDto(step);
        assertThat(stepSnapshot.getId()).isEqualTo(2L);
        assertThat(stepSnapshot.getName()).isEqualTo("Wash");
        assertThat(stepSnapshot.getDeleted()).isTrue();
    }
}
