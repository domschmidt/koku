package de.domschmidt.koku.user.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
import de.domschmidt.koku.dto.user.KokuUserDto;
import de.domschmidt.koku.user.kafka.users.transformer.UserAppointmentToKafkaUserAppointmentDtoTransformer;
import de.domschmidt.koku.user.kafka.users.transformer.UserToKafkaUserDtoTransformer;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserAppointment;
import de.domschmidt.koku.user.persistence.UserRegion;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class UserTransformerTest {

    private final EntityManager entityManager = mock(EntityManager.class);

    @Test
    void userRoundTripMapsNamesMetadataAndRegion() {
        final UserRegion region = new UserRegion();
        region.setId(7L);
        final User user = new User();
        user.setId("u-1");
        user.setVersion(4L);
        user.setFirstname("Ada");
        user.setLastname("Lovelace");
        user.setAvatarBase64("avatar");
        user.setRegion(region);
        user.setDeleted(true);

        final KokuUserDto dto = new UserToKokuUserDtoTransformer(entityManager).transformToDto(user);

        assertThat(dto.getId()).isEqualTo("u-1");
        assertThat(dto.getFullname()).isEqualTo("Ada Lovelace");
        assertThat(dto.getInitials()).isEqualTo("AL");
        assertThat(dto.getRegionId()).isEqualTo(7L);
        assertThat(dto.getDeleted()).isTrue();
    }

    @Test
    void regionTransformerMapsCountryAndStateCodes() {
        final UserRegion region = new UserRegion();
        region.setId(7L);
        region.setCountryIso("DE");
        region.setStateIso("BE");

        final var dto = new UserRegionToKokuUserRegionDtoTransformer().transformToDto(region);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getCountry()).isEqualTo("DE");
        assertThat(dto.getState()).isEqualTo("BE");
    }

    @Test
    void userUpdateAppliesProvidedValuesAndResolvesRegion() {
        final User user = new User();
        final UserRegion region = new UserRegion();
        when(entityManager.getReference(UserRegion.class, 8L)).thenReturn(region);
        final KokuUserDto update = KokuUserDto.builder()
                .firstname("Grace")
                .lastname("Hopper")
                .avatarBase64("new-avatar")
                .regionId(8L)
                .deleted(true)
                .build();

        new UserToKokuUserDtoTransformer(entityManager).transformToEntity(user, update);

        assertThat(user.getFirstname()).isEqualTo("Grace");
        assertThat(user.getLastname()).isEqualTo("Hopper");
        assertThat(user.getAvatarBase64()).isEqualTo("new-avatar");
        assertThat(user.getRegion()).isSameAs(region);
        assertThat(user.isDeleted()).isTrue();
        verify(entityManager).getReference(UserRegion.class, 8L);
    }

    @Test
    void absentOptionalUserValuesPreserveFieldsButClearRegion() {
        final User user = new User("Existing", "Name", "avatar");
        user.setRegion(new UserRegion());

        new UserToKokuUserDtoTransformer(entityManager)
                .transformToEntity(user, KokuUserDto.builder().build());

        assertThat(user.getFirstname()).isEqualTo("Existing");
        assertThat(user.getLastname()).isEqualTo("Name");
        assertThat(user.getAvatarBase64()).isEqualTo("avatar");
        assertThat(user.getRegion()).isNull();
    }

    @Test
    void appointmentRoundTripMapsDateTimeUserAndSummary() {
        final User user = new User("u-2");
        user.setFirstname("Katherine");
        user.setLastname("Johnson");
        final UserAppointment appointment = new UserAppointment();
        appointment.setId(12L);
        appointment.setVersion(3L);
        appointment.setUser(user);
        appointment.setDescription("Research");
        appointment.setStartTimestamp(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 15));
        appointment.setEndTimestamp(LocalDateTime.of(2026, java.time.Month.JULY, 12, 10, 45));

        final KokuUserAppointmentDto dto =
                new UserAppointmentToUserAppointmentDtoTransformer(entityManager).transformToDto(appointment);

        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2026, java.time.Month.JULY, 12));
        assertThat(dto.getStartTime()).isEqualTo(LocalTime.of(9, 15));
        assertThat(dto.getEndTime()).isEqualTo(LocalTime.of(10, 45));
        assertThat(dto.getUserName()).isEqualTo("Katherine Johnson");
        assertThat(dto.getSummary()).isEqualTo("Privater Termin vom 12.07.2026");
    }

    @Test
    void appointmentWithoutTimestampsMapsNullableDateFields() {
        final UserAppointment appointment = new UserAppointment();
        appointment.setUser(new User("u-2"));

        final KokuUserAppointmentDto dto =
                new UserAppointmentToUserAppointmentDtoTransformer(entityManager).transformToDto(appointment);

        assertThat(dto.getStartDate()).isNull();
        assertThat(dto.getStartTime()).isNull();
        assertThat(dto.getEndDate()).isNull();
        assertThat(dto.getEndTime()).isNull();
        assertThat(dto.getSummary()).isNull();
    }

    @Test
    void appointmentUpdateCombinesCompleteDateTimesAndResolvesUser() {
        final UserAppointment appointment = new UserAppointment();
        final User user = new User("u-3");
        when(entityManager.getReference(User.class, "u-3")).thenReturn(user);
        final KokuUserAppointmentDto update = KokuUserAppointmentDto.builder()
                .startDate(LocalDate.of(2026, java.time.Month.AUGUST, 1))
                .startTime(LocalTime.of(11, 30))
                .endDate(LocalDate.of(2026, java.time.Month.AUGUST, 1))
                .endTime(LocalTime.NOON)
                .description("Planning")
                .userId("u-3")
                .deleted(true)
                .build();

        new UserAppointmentToUserAppointmentDtoTransformer(entityManager).transformToEntity(appointment, update);

        assertThat(appointment.getStartTimestamp())
                .isEqualTo(LocalDateTime.of(2026, java.time.Month.AUGUST, 1, 11, 30));
        assertThat(appointment.getEndTimestamp()).isEqualTo(LocalDateTime.of(2026, java.time.Month.AUGUST, 1, 12, 0));
        assertThat(appointment.getDescription()).isEqualTo("Planning");
        assertThat(appointment.getUser()).isSameAs(user);
        assertThat(appointment.isDeleted()).isTrue();
    }

    @Test
    void incompleteAppointmentUpdateDoesNotOverwriteExistingValues() {
        final UserAppointment appointment = new UserAppointment();
        final LocalDateTime originalStart = LocalDateTime.of(2026, java.time.Month.JANUARY, 1, 8, 0);
        appointment.setStartTimestamp(originalStart);
        appointment.setDescription("Existing");

        new UserAppointmentToUserAppointmentDtoTransformer(entityManager)
                .transformToEntity(
                        appointment,
                        KokuUserAppointmentDto.builder()
                                .startDate(LocalDate.of(2026, java.time.Month.FEBRUARY, 1))
                                .endTime(LocalTime.NOON)
                                .build());

        assertThat(appointment.getStartTimestamp()).isEqualTo(originalStart);
        assertThat(appointment.getEndTimestamp()).isNull();
        assertThat(appointment.getDescription()).isEqualTo("Existing");
    }

    @Test
    void kafkaSnapshotsContainRegionAndAppointmentOwnership() {
        final UserRegion region = new UserRegion();
        region.setCountryIso("DE");
        region.setStateIso("BE");
        final User user = new User("u-1");
        user.setFirstname("Ada");
        user.setLastname("Lovelace");
        user.setAvatarBase64("avatar");
        user.setRegion(region);
        user.setDeleted(true);

        final var userSnapshot = new UserToKafkaUserDtoTransformer().transformToDto(user);
        assertThat(userSnapshot.getId()).isEqualTo("u-1");
        assertThat(userSnapshot.getCountryIso()).isEqualTo("DE");
        assertThat(userSnapshot.getStateIso()).isEqualTo("BE");
        assertThat(userSnapshot.getDeleted()).isTrue();

        final UserAppointment appointment = new UserAppointment();
        appointment.setId(3L);
        appointment.setUser(user);
        appointment.setDescription("Planning");
        appointment.setStartTimestamp(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0));
        appointment.setEndTimestamp(LocalDateTime.of(2026, java.time.Month.JULY, 12, 10, 0));
        appointment.setDeleted(true);
        final var appointmentSnapshot =
                new UserAppointmentToKafkaUserAppointmentDtoTransformer().transformToDto(appointment);
        assertThat(appointmentSnapshot.getUserId()).isEqualTo("u-1");
        assertThat(appointmentSnapshot.getDescription()).isEqualTo("Planning");
        assertThat(appointmentSnapshot.getDeleted()).isTrue();
    }
}
