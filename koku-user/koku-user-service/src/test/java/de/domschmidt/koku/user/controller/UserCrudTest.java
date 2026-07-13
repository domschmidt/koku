package de.domschmidt.koku.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.user.KokuUserDto;
import de.domschmidt.koku.user.kafka.users.service.UserKafkaService;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserRepository;
import de.domschmidt.koku.user.transformer.UserToKokuUserDtoTransformer;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

class UserCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final UserRepository repository = mock(UserRepository.class);
    private final UserToKokuUserDtoTransformer transformer = mock(UserToKokuUserDtoTransformer.class);
    private final UserKafkaService kafkaService = mock(UserKafkaService.class);
    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(entityManager, repository, transformer, kafkaService);
    }

    @Test
    void readTransformsExistingUser() {
        final User user = user("u-1", 2L, false);
        final KokuUserDto dto = KokuUserDto.builder().id("u-1").build();
        when(repository.findById("u-1")).thenReturn(Optional.of(user));
        when(transformer.transformToDto(user)).thenReturn(dto);

        assertThat(controller.read("u-1")).isSameAs(dto);
    }

    @Test
    void readRejectsUnknownUser() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.read("missing"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateTransformsFlushesPublishesAndReturnsDto() throws Exception {
        final User user = user("u-1", 2L, false);
        final KokuUserDto update = KokuUserDto.builder().version(2L).build();
        final KokuUserDto result = KokuUserDto.builder().id("u-1").build();
        when(repository.findById("u-1")).thenReturn(Optional.of(user));
        when(transformer.transformToDto(user)).thenReturn(result);

        assertThat(controller.updateOrCreate("u-1", false, update)).isSameAs(result);
        verify(transformer).transformToEntity(user, update);
        verify(entityManager).flush();
        verify(kafkaService).sendUser(user);
    }

    @Test
    void updateRejectsStaleVersionWithoutMutation() {
        final User user = user("u-1", 3L, false);
        when(repository.findById("u-1")).thenReturn(Optional.of(user));
        final KokuUserDto staleUpdate = KokuUserDto.builder().version(2L).build();

        assertThatThrownBy(() -> controller.updateOrCreate("u-1", false, staleUpdate))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
    }

    @Test
    void forcedUpdateAcceptsStaleVersion() {
        final User user = user("u-1", 3L, false);
        final KokuUserDto update = KokuUserDto.builder().version(2L).build();
        when(repository.findById("u-1")).thenReturn(Optional.of(user));

        controller.updateOrCreate("u-1", true, update);

        verify(transformer).transformToEntity(user, update);
    }

    @Test
    void updateEndpointCreatesMissingUser() {
        final KokuUserDto update = KokuUserDto.builder().version(0L).build();
        final User created = user("u-new", 0L, false);
        when(repository.findById("u-new")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(created);

        controller.updateOrCreate("u-new", false, update);

        verify(repository).save(any(User.class));
    }

    @Test
    void deleteAndRestoreToggleLifecycleAndPublish() throws Exception {
        final User user = user("u-1", 1L, false);
        when(entityManager.getReference(User.class, "u-1")).thenReturn(user);

        controller.delete("u-1");
        assertThat(user.isDeleted()).isTrue();
        controller.restore("u-1");
        assertThat(user.isDeleted()).isFalse();
        verify(kafkaService, times(2)).sendUser(user);
    }

    @Test
    void lifecycleRejectsRepeatedOperations() {
        final User user = user("u-1", 1L, true);
        when(entityManager.getReference(User.class, "u-1")).thenReturn(user);

        assertThatThrownBy(() -> controller.delete("u-1")).isInstanceOf(ResponseStatusException.class);
        user.setDeleted(false);
        assertThatThrownBy(() -> controller.restore("u-1")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createPersistsPublishesAndTransforms() throws Exception {
        final KokuUserDto input = KokuUserDto.builder().firstname("New").build();
        final User entity = user("u-new", 0L, false);
        final KokuUserDto result = KokuUserDto.builder().id("u-new").build();
        when(transformer.transformToEntity(any(User.class), org.mockito.Mockito.same(input)))
                .thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(transformer.transformToDto(entity)).thenReturn(result);

        assertThat(controller.create(input)).isSameAs(result);
        verify(kafkaService).sendUser(entity);
    }

    @Test
    void kafkaExecutionFailureIsExposedAsServerError() throws Exception {
        final User user = user("u-1", 1L, false);
        when(kafkaService.sendUser(user)).thenThrow(new ExecutionException(new IllegalStateException("broker")));

        assertThatThrownBy(() -> controller.sendUserUpdate(user))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendUser(user);
        assertThatThrownBy(() -> controller.sendUserUpdate(user)).isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    @Test
    void selfServiceReadsAndUpdatesAuthenticatedUser() throws Exception {
        final User user = user("u-1", 2L, false);
        final KokuUserDto result = KokuUserDto.builder().id("u-1").build();
        final KokuUserDto update = KokuUserDto.builder().version(2L).build();
        when(repository.findById("u-1")).thenReturn(Optional.of(user));
        when(transformer.transformToDto(user)).thenReturn(result);
        final Jwt jwt = jwt("u-1", null, null, null);

        assertThat(controller.getMyDetails(jwt)).isSameAs(result);
        controller.updateMyDetails(update, jwt);

        verify(transformer).transformToEntity(user, update);
        verify(kafkaService).sendUser(user);
    }

    @Test
    void syncCreatesMissingUserAndCopiesIdentityClaims() throws Exception {
        final User created = new User("u-new");
        when(repository.findById("u-new")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(created);

        controller.syncMyDetails(jwt("u-new", "Ada", "Lovelace", "Ada Lovelace"));

        assertThat(created.getFirstname()).isEqualTo("Ada");
        assertThat(created.getLastname()).isEqualTo("Lovelace");
        assertThat(created.getFullname()).isEqualTo("Ada Lovelace");
        verify(kafkaService).sendUser(created);
    }

    @Test
    void syncUpdatesChangedClaimsButSkipsUnchangedAndAbsentClaims() throws Exception {
        final User user = user("u-1", 2L, false);
        user.setFirstname("Ada");
        user.setLastname("Lovelace");
        user.setFullname("Old Name");
        when(repository.findById("u-1")).thenReturn(Optional.of(user));

        controller.syncMyDetails(jwt("u-1", "Ada", "Lovelace", "Ada Lovelace"));
        assertThat(user.getFullname()).isEqualTo("Ada Lovelace");
        verify(kafkaService).sendUser(user);

        controller.syncMyDetails(jwt("u-1", null, null, null));
        verify(kafkaService, times(1)).sendUser(user);
    }

    @Test
    void summaryUsesExistingUserAndJwtSubjectIsRequired() {
        final User user = user("u-1", 2L, false);
        user.setFirstname("Ada");
        user.setLastname("Lovelace");
        when(repository.findById("u-1")).thenReturn(Optional.of(user));
        final Jwt jwtWithoutSubject = jwt(null, null, null, null);

        assertThat(controller.readSummary("u-1").getSummary()).isEqualTo("Ada Lovelace");
        assertThatThrownBy(() -> controller.getMyDetails(jwtWithoutSubject))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        assertThatThrownBy(() -> controller.getMyDetails(null)).isInstanceOf(ResponseStatusException.class);
    }

    private static Jwt jwt(String subject, String givenName, String familyName, String name) {
        final Jwt.Builder builder =
                Jwt.withTokenValue("token").header("alg", "none").claim("scope", "openid");
        if (subject != null) {
            builder.subject(subject);
        }
        if (givenName != null) {
            builder.claim("given_name", givenName);
        }
        if (familyName != null) {
            builder.claim("family_name", familyName);
        }
        if (name != null) {
            builder.claim("name", name);
        }
        return builder.build();
    }

    private static User user(String id, Long version, boolean deleted) {
        final User user = new User(id);
        user.setVersion(version);
        user.setDeleted(deleted);
        return user;
    }
}
